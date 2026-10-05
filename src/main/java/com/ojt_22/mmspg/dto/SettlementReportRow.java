package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SettlementReportRow {
    private String settlementId, settlementReference, transactionReference, bankAccountNo, status;
    private LocalDate settlementDate;
    private BigDecimal grossAmount, feeAmount, netAmount;
    private LocalDateTime processedAt, createdAt;
}
