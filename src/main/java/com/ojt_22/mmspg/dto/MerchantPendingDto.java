package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class MerchantPendingDto {
    private UUID merchantId;
    private String merchantCode;
    private String businessName;
    private String email;
    private String businessType;
    private LocalDateTime createdAt;
    private String status;
}
