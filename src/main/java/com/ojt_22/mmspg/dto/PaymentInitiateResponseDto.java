package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentInitiateResponseDto {
    private String transactionReference;
    private String paymentToken;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentUrl;
}