package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentTransactionSummaryDto {
    private String transactionReference;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime initiatedAt;
}