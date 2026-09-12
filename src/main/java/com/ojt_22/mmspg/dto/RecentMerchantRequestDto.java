package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class RecentMerchantRequestDto {
    private UUID requestId;
    private String merchantName;
    private String businessType;
    private LocalDateTime date;
    private String status;
}
