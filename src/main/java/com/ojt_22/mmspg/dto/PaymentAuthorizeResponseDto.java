package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizeResponseDto {
    private String transactionReference;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;           // COMPLETED သို့မဟုတ် FAILED
    private String failureReason;    // Fail ဖြစ်ခဲ့ပါက ဖော်ပြရန် အကြောင်းရင်း
    private LocalDateTime completedAt;
}