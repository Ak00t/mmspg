package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.util.UUID;
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
public class BranchSalesSummaryDto {
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private long transactionCount;
    private BigDecimal totalVolume;
}
