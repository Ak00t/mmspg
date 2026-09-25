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
import org.springframework.validation.annotation.Validated;

import com.ojt_22.mmspg.dto.CreateWebhookRequest;
import com.ojt_22.mmspg.dto.UpdateWebhookRequest;
import com.ojt_22.mmspg.dto.WebhookConfigResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.entity.WebhookConfig;
import com.ojt_22.mmspg.enums.WebhookConfigStatus;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.repository.WebhookConfigRepository;
import com.ojt_22.mmspg.service.WebhookConfigService;
import com.ojt_22.mmspg.utils.CredentialUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated // Spring Validation AOP အသက်သွင်းခြင်း
@RequiredArgsConstructor
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public WebhookConfigResponse createWebhook(@Valid CreateWebhookRequest request) {
        // @Valid ကြောင့် request ထဲရှိ Not-Null fields များ မပြည့်စုံပါက ဤနေရာသို့ မရောက်မီ အလိုအလျောက် Exception တက်ပါမည်

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found: " + request.getMerchantId()));

        // created_by အတွက် Staff User ကို ရှာဖွေရယူခြင်း[cite: 12]
        UUID defaultStaffId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        StaffUser createdByStaff = staffUserRepository.findById(defaultStaffId)
                .orElseGet(() -> staffUserRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("No staff user found to assign created_by")));

        String rawSecretKey = CredentialUtils.generateWebhookSecret();

        WebhookConfig config = new WebhookConfig();
        config.setMerchant(merchant);
        config.setCreatedBy(createdByStaff);
        config.setCallbackUrl(request.getCallbackUrl());
        config.setSecretKeyHash(passwordEncoder.encode(rawSecretKey));
        config.setEventPaymentCompleted(request.getEventPaymentCompleted());
        config.setEventPaymentFailed(request.getEventPaymentFailed());
        config.setMaxRetry(request.getMaxRetry());
        config.setDescription(request.getDescription());
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
                .status(saved.getStatus().name())
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
                .status(updated.getStatus().name())
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteWebhook(UUID webhookId) {
        WebhookConfig config = webhookConfigRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook config not found: " + webhookId));

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

            HttpResponse response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return "Test Webhook sent. Remote status code: " + response.statusCode();
        } catch (Exception e) {
            return "Test Webhook failed to connect: " + e.getMessage();
        }
    }
}