package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SettlementReportRow {
    private String merchantBusinessName, merchantEmail, merchantPhone;
    private String settlementId, settlementReference, transactionReference, bankAccountNo, status;
    private LocalDate settlementDate;
    private BigDecimal grossAmount, feeAmount, netAmount;
    private LocalDateTime processedAt, createdAt;

    public String getFormattedSettlementDate() {
        return settlementDate == null ? "" : settlementDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String getFormattedProcessedAt() {
        return processedAt == null ? "" : processedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
}
