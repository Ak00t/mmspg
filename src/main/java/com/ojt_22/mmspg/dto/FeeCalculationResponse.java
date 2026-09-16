package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeeCalculationResponse {

    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
}