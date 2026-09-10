package com.ojt_22.mmspg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreBankingResponseDto {
    private boolean success;
    private String coreTransactionRef;
    private String failureReason;
}