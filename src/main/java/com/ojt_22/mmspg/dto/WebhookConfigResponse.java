package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfigResponse {
    private UUID webhookId;
    private UUID merchantId;
    private String callbackUrl;
    private String rawSecretKey; // ဖန်တီးချိန်တွင်သာ Plain Secret ပြန်ပေးမည်
    private Boolean eventPaymentCompleted;
    private Boolean eventPaymentFailed;
    private Integer maxRetry;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}