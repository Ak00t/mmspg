package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformLiveTransactionDto {
    private String transactionReference;
    private String orderId;
    private String merchantName;
    private String merchantCode;
    private String branchName;
    private String branchCode;
    private String terminalCode;
    private String terminalType;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
