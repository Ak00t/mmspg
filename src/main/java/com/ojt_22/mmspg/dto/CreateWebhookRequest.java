package com.ojt_22.mmspg.dto;

import java.util.UUID;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWebhookRequest {

    @NotNull(message = "Merchant ID is required.")
    private UUID merchantId;

    @NotBlank(message = "Callback URL must not be blank.")
    @URL(protocol = "https", message = "Callback URL must be a valid HTTPS URL.")
    @Size(max = 500, message = "Callback URL must not exceed 500 characters.")
    private String callbackUrl;

    @NotNull(message = "eventPaymentCompleted setting is required.")
    private Boolean eventPaymentCompleted;

    @NotNull(message = "eventPaymentFailed setting is required.")
    private Boolean eventPaymentFailed;

    @NotNull(message = "Max retry count is required.")
    @Min(value = 1, message = "Max retry must be at least 1.")
    @Max(value = 10, message = "Max retry must not exceed 10.")
    private Integer maxRetry;

    @Size(max = 255, message = "Description must not exceed 255 characters.")
    private String description;
}