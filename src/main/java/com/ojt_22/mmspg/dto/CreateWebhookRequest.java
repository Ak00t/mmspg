package com.ojt_22.mmspg.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateWebhookRequest {
	
	private UUID merchantId;
    private String callbackUrl;
    private Boolean eventPaymentCompleted;
    private Boolean eventPaymentFailed;
    private Integer maxRetry;
    private String description;
}
