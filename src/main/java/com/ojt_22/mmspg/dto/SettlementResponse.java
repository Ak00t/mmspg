package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ojt_22.mmspg.enums.SettlementStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettlementResponse {

    private UUID settlementId;
    private UUID transactionId;
    private UUID merchantId;

    private String settlementReference;
    private LocalDate settlementDate;

    private BigDecimal grossAmount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;

    private String bankAccountNo;
    private SettlementStatus status;
    private LocalDateTime processedAt;
}