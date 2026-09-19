package com.ojt_22.mmspg.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.CreateWebhookRequest;
import com.ojt_22.mmspg.dto.UpdateWebhookRequest;
import com.ojt_22.mmspg.dto.WebhookConfigResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.WebhookConfig;
import com.ojt_22.mmspg.enums.WebhookConfigStatus;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.WebhookConfigRepository;
import com.ojt_22.mmspg.service.WebhookConfigService;
import com.ojt_22.mmspg.utils.CredentialUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public WebhookConfigResponse createWebhook(CreateWebhookRequest request) {
        if (request == null || request.getMerchantId() == null) {
            throw new IllegalArgumentException("Merchant ID must not be null.");
        }

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found: " + request.getMerchantId()));

        String rawSecretKey = CredentialUtils.generateWebhookSecret();

        WebhookConfig config = new WebhookConfig();
        config.setMerchant(merchant);
        config.setCallbackUrl(request.getCallbackUrl());
        config.setSecretKeyHash(passwordEncoder.encode(rawSecretKey));
        config.setEventPaymentCompleted(request.getEventPaymentCompleted() != null ? request.getEventPaymentCompleted() : true);
        config.setEventPaymentFailed(request.getEventPaymentFailed() != null ? request.getEventPaymentFailed() : true);
        config.setMaxRetry(request.getMaxRetry() != null ? request.getMaxRetry() : 3);
        config.setDescription(request.getDescription());
        
        // String "ACTIVE" အစား Enum Type ကို အသုံးပြုထားပါသည်
        config.setStatus(WebhookConfigStatus.ACTIVE);

        WebhookConfig saved = webhookConfigRepository.save(config);

        return WebhookConfigResponse.builder()
                .webhookId(saved.getId())
                .merchantId(saved.getMerchant().getId())
                .callbackUrl(saved.getCallbackUrl())
                .rawSecretKey(rawSecretKey)
                .eventPaymentCompleted(saved.getEventPaymentCompleted())
                .eventPaymentFailed(saved.getEventPaymentFailed())
                .maxRetry(saved.getMaxRetry())
                .description(saved.getDescription())
                .status(saved.getStatus().name()) // Enum မှ String သို့ ပြောင်းလဲခြင်း[cite: 9, 10]
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public WebhookConfigResponse updateWebhook(UUID webhookId, UpdateWebhookRequest request) {
        WebhookConfig config = webhookConfigRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook config not found: " + webhookId));

        if (request.getCallbackUrl() != null) config.setCallbackUrl(request.getCallbackUrl());
        if (request.getEventPaymentCompleted() != null) config.setEventPaymentCompleted(request.getEventPaymentCompleted());
        if (request.getEventPaymentFailed() != null) config.setEventPaymentFailed(request.getEventPaymentFailed());
        if (request.getMaxRetry() != null) config.setMaxRetry(request.getMaxRetry());
        if (request.getDescription() != null) config.setDescription(request.getDescription());
        
        // String မှ Enum သို့ valueOf ဖြင့် ပြောင်းလဲထည့်သွင်းခြင်း[cite: 9, 10]
        if (request.getStatus() != null) {
            config.setStatus(WebhookConfigStatus.valueOf(request.getStatus().trim().toUpperCase()));
        }

        WebhookConfig updated = webhookConfigRepository.save(config);

        return WebhookConfigResponse.builder()
                .webhookId(updated.getId())
                .merchantId(updated.getMerchant().getId())
                .callbackUrl(updated.getCallbackUrl())
                .rawSecretKey("********")
                .eventPaymentCompleted(updated.getEventPaymentCompleted())
                .eventPaymentFailed(updated.getEventPaymentFailed())
                .maxRetry(updated.getMaxRetry())
                .description(updated.getDescription())
                .status(updated.getStatus().name()) // Enum မှ String သို့ ပြောင်းလဲခြင်း[cite: 9, 10]
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteWebhook(UUID webhookId) {
        WebhookConfig config = webhookConfigRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook config not found: " + webhookId));

        // String "INACTIVE" အစား Enum Type ကို အသုံးပြုထားပါသည်[cite: 9, 10]
        config.setStatus(WebhookConfigStatus.INACTIVE);
        webhookConfigRepository.save(config);
    }

    @Override
    public String testWebhook(UUID webhookId) {
        WebhookConfig config = webhookConfigRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook config not found: " + webhookId));

        String testPayload = "{\"event\": \"TEST_PING\", \"timestamp\": \"" + LocalDateTime.now() + "\"}";

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getCallbackUrl()))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "MMSPG-Webhook-Tester/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(testPayload))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return "Test Webhook sent. Remote status code: " + response.statusCode();
        } catch (Exception e) {
            return "Test Webhook failed to connect: " + e.getMessage();
        }
    }
}