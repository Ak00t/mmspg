package com.ojt_22.mmspg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWebhookRequest {
    private String callbackUrl;
    private Boolean eventPaymentCompleted;
    private Boolean eventPaymentFailed;
    private Integer maxRetry;
    private String description;
    private String status; // ACTIVE or INACTIVE
}