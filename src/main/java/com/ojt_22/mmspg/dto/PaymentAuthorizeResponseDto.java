package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentAuthorizeResponseDto {
    private String transactionReference;
    private String coreTransactionReference;
    private String status;
    private LocalDateTime completedAt;
    private String failureReason;
}