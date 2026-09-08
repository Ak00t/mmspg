package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentInitiateRequestDto {
    private UUID merchantId;
    private UUID branchId;
    private UUID terminalId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
}