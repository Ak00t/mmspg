package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class MerchantFeeResponseDto {
    private UUID feeId;
    private UUID merchantId;
    private String merchantName;
    private String feeType;
    private BigDecimal feePercentage;
    private BigDecimal flatAmount;
    private String status;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
}
