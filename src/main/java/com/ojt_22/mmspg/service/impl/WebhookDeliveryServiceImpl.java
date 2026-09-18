package com.ojt_22.mmspg.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.WebhookDeliveryDto;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.entity.WebhookConfig;
import com.ojt_22.mmspg.entity.WebhookDelivery;
import com.ojt_22.mmspg.enums.WebhookConfigStatus;
import com.ojt_22.mmspg.enums.WebhookDeliveryStatus;
import com.ojt_22.mmspg.repository.WebhookConfigRepository;
import com.ojt_22.mmspg.repository.WebhookDeliveryRepository;
import com.ojt_22.mmspg.service.WebhookDeliveryService;
import com.ojt_22.mmspg.utils.ApiLogUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebhookDeliveryServiceImpl implements WebhookDeliveryService {

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookConfigRepository webhookConfigRepository;

    @Override
    @Transactional
    public void sendWebhook(PaymentTransaction transaction, String eventType) {
        // Merchant ၏ Active ဖြစ်နေသော Webhook Config ကို ရှာဖွေခြင်း[cite: 14]
        Optional<WebhookConfig> configOpt = webhookConfigRepository
                .findByMerchantIdAndStatus(transaction.getMerchant().getId(), WebhookConfigStatus.ACTIVE.name());

        if (configOpt.isEmpty()) {
            return; // Config မရှိပါက ပို့ရန် မလိုပါ
        }

        WebhookConfig config = configOpt.get();

        // Event Subscription စစ်ဆေးခြင်း[cite: 14]
        if ("PAYMENT_COMPLETED".equalsIgnoreCase(eventType) && !Boolean.TRUE.equals(config.getEventPaymentCompleted())) {
            return;
        }
        if ("PAYMENT_FAILED".equalsIgnoreCase(eventType) && !Boolean.TRUE.equals(config.getEventPaymentFailed())) {
            return;
        }

        // Webhook Payload တည်ဆောက်ခြင်း[cite: 14]
        String payload = String.format(
                "{\"event\":\"%s\",\"transactionId\":\"%s\",\"amount\":%s,\"currency\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                eventType,
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                LocalDateTime.now()
        );

        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setWebhook(config);
        delivery.setTransaction(transaction);
        delivery.setEventType(eventType);
        delivery.setPayload(payload);
        delivery.setAttemptCount(0);
        delivery.setStatus(WebhookDeliveryStatus.PENDING);
        delivery.setNextRetryAt(LocalDateTime.now()); // ချက်ချင်းပို့ရန် သတ်မှတ်ခြင်း[cite: 14]

        WebhookDelivery saved = webhookDeliveryRepository.save(delivery);

        // Async အနေဖြင့် ချက်ချင်း ပို့ဆောင်ခြင်း[cite: 14]
        executeDeliveryAsync(saved.getId());
    }

    @Async
    public void executeDeliveryAsync(UUID deliveryId) {
        executeDelivery(deliveryId);
    }

    @Override
    @Transactional
    public void executeDelivery(UUID deliveryId) {
        WebhookDelivery delivery = webhookDeliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getStatus() == WebhookDeliveryStatus.DELIVERED) {
            return;
        }

        WebhookConfig config = delivery.getWebhook();
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setSentAt(LocalDateTime.now());

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getCallbackUrl()))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "MMSPG-Webhook-Engine/1.0")
                    .header("X-Event-Type", delivery.getEventType())
                    .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayload()))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            delivery.setResponseStatus(response.statusCode());
            delivery.setResponseBody(ApiLogUtils.truncate(response.body(), 2000));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.setStatus(WebhookDeliveryStatus.DELIVERED);
                delivery.setDeliveredAt(LocalDateTime.now());
                delivery.setErrorMessage(null);
            } else {
                handleFailure(delivery, config.getMaxRetry(), "Remote server responded with HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            delivery.setResponseStatus(null);
            handleFailure(delivery, config.getMaxRetry(), "Connection failed: " + e.getMessage());
        }

        webhookDeliveryRepository.save(delivery);
    }

    private void handleFailure(WebhookDelivery delivery, int maxRetry, String error) {
        delivery.setErrorMessage(ApiLogUtils.truncate(error, 1000));
        if (delivery.getAttemptCount() >= maxRetry) {
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
        } else {
            delivery.setStatus(WebhookDeliveryStatus.PENDING);
            // Exponential Backoff: ကြိုးစားမှုအလိုက် အချိန်ပိုခွာ၍ ပို့ခြင်း (ဥပမာ- 1 min, 4 min, 9 min)[cite: 14]
            int delayMinutes = (int) Math.pow(delivery.getAttemptCount(), 2);
            delivery.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDeliveryDto> getDeliveriesByMerchant(UUID merchantId, Pageable pageable) {
        return webhookDeliveryRepository.findByMerchantId(merchantId, pageable)
                .map(d -> WebhookDeliveryDto.builder()
                        .deliveryId(d.getId())
                        .transactionId(d.getTransaction().getId())
                        .eventType(d.getEventType())
                        .payload(d.getPayload())
                        .responseStatus(d.getResponseStatus())
                        .attemptCount(d.getAttemptCount())
                        .status(d.getStatus().name())
                        .sentAt(d.getSentAt())
                        .deliveredAt(d.getDeliveredAt())
                        .errorMessage(d.getErrorMessage())
                        .nextRetryAt(d.getNextRetryAt())
                        .build());
    }

    @Override
    @Transactional
    public void redeliver(UUID deliveryId) {
        WebhookDelivery delivery = webhookDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
        delivery.setStatus(WebhookDeliveryStatus.PENDING);
        delivery.setNextRetryAt(LocalDateTime.now());
        webhookDeliveryRepository.save(delivery);
        executeDelivery(deliveryId);
    }
}