package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreBankingDebitRequestDto {
    private String customerId;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private String transactionReference;
}