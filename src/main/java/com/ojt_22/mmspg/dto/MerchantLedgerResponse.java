package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantLedgerResponse {

    private UUID ledgerId;
    private UUID merchantId;
    private UUID transactionId;

    private BigDecimal amount;

    private String entryType;
    private String balanceType;

    private String description;

    private LocalDateTime createdAt;
}