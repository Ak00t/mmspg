package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantFeeRequestDto {
    
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;

    @NotBlank(message = "Fee Type is required (PERCENTAGE, FLAT, MIXED)")
    private String feeType;

    private BigDecimal feePercentage;
    
    private BigDecimal flatAmount;
}
