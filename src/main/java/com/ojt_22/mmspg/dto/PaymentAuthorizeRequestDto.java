package com.ojt_22.mmspg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizeRequestDto {

    @NotBlank(message = "Payment token is required")
    private String paymentToken;

    private String customerId;

    private String sourceAccountNumber;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String failureReason;

    private String errorCode;

    private String errorMessage;
}