package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class TerminalResponseDto {
    private UUID terminalId;
    private String terminalCode;
    private String terminalName;
    private String terminalType;
    private UUID merchantId;
    private String merchantName;
    private UUID branchId;
    private String branchName;
    private String status;
    private LocalDateTime createdAt;
}
