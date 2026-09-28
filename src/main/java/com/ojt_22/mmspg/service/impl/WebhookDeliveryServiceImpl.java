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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojt_22.mmspg.dto.WebhookDeliveryDto;
import com.ojt_22.mmspg.dto.WebhookPayloadDto;
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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryServiceImpl implements WebhookDeliveryService {

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void sendWebhook(PaymentTransaction transaction, String eventType) {
        Optional<WebhookConfig> configOpt = webhookConfigRepository
                .findByMerchantIdAndStatus(transaction.getMerchant().getId(), WebhookConfigStatus.ACTIVE.name());

        if (configOpt.isEmpty()) {
            return;
        }

        // Explicit Type Cast ပြုလုပ်ထားသဖြင့် configOpt.get() တွင် အနီရောင်မျဉ်း လုံးဝ မပြတော့ပါ[cite: 30]
        WebhookConfig config = (WebhookConfig) configOpt.get();

        if ("PAYMENT_COMPLETED".equalsIgnoreCase(eventType) && !Boolean.TRUE.equals(config.getEventPaymentCompleted())) {
            return;
        }
        if ("PAYMENT_FAILED".equalsIgnoreCase(eventType) && !Boolean.TRUE.equals(config.getEventPaymentFailed())) {
            return;
        }

        // WebhookPayloadDto နှင့် ObjectMapper ဖြင့် Valid JSON Format တည်ဆောက်ခြင်း[cite: 29]
        String payload;
        try {
            WebhookPayloadDto payloadDto = WebhookPayloadDto.builder()
                    .event(eventType)
                    .transactionId(transaction.getId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                    .timestamp(LocalDateTime.now())
                    .build();

            payload = objectMapper.writeValueAsString(payloadDto);
        } catch (JsonProcessingException e) {
            log.error("Failed to generate webhook payload JSON for transaction: {}", transaction.getId(), e);
            throw new IllegalStateException("Error serializing webhook payload", e);
        }

        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setWebhook(config);
        delivery.setTransaction(transaction);
        delivery.setEventType(eventType);
        delivery.setPayload(payload);
        delivery.setAttemptCount(0);
        delivery.setStatus(WebhookDeliveryStatus.PENDING);
        delivery.setNextRetryAt(LocalDateTime.now());

        WebhookDelivery saved = webhookDeliveryRepository.save(delivery);

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

            // HttpResponse generic type ကို သတ်မှတ်ထားပါသည်[cite: 31]
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            delivery.setResponseStatus(response.statusCode());
            
            // String.valueOf ဖြင့် သေချာစွာ String သို့ ပြောင်းပြီးမှ truncate ခေါ်ထားသဖြင့် အနီရောင်မျဉ်း မတက်တော့ပါ[cite: 31]
            String responseBodyText = response.body() != null ? String.valueOf(response.body()) : "";
            delivery.setResponseBody(ApiLogUtils.truncate(responseBodyText, 2000));

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
            int delayMinutes = (int) Math.pow(delivery.getAttemptCount(), 2);
            delivery.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page getDeliveriesByMerchant(UUID merchantId, Pageable pageable) {
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