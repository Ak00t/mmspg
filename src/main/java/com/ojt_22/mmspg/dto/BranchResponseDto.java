package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class BranchResponseDto {
    private UUID branchId;
    private UUID merchantId;
    private String merchantName;
    private String branchCode;
    private String branchName;
    private String city;
    private String address;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
}
