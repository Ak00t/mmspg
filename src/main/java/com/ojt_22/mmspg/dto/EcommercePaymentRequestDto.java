package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcommercePaymentRequestDto {

    private String sourceAccountNumber;
    private String merchantAccountNumber;
    private BigDecimal amount;
    private BigDecimal serviceFee;
    @Builder.Default
    private String currency = "MMK";
    private String orderId;
    private String idempotencyKey;
    private String description;
}
