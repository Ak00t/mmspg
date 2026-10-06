package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SalesLedgerReportRow {
    private String ledgerId, transactionReference, orderId, entryType, balanceType, description;
    private String transactionStatus, settlementReference;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDate settlementDate;
}
