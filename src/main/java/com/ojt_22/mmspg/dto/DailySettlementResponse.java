package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailySettlementResponse {

    private LocalDate settlementDate;

    private int transactionCount;

    private BigDecimal grossAmount;

    private BigDecimal totalFees;

    private BigDecimal netSettlement;
}