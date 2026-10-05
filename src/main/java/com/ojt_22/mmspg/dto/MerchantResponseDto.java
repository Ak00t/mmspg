package com.ojt_22.mmspg.dto;

import java.util.UUID;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantResponseDto {
    private UUID merchantId;
    private String merchantCode;
    private String merchantName;
    private String email;
    private LocalDateTime createdAt;
    private String riskLevel;
    private String status;
}