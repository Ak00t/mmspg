package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DashboardSummaryResponse {
    private long totalPendingApprovals;
    private BigDecimal totalVolumeToday;
    private long activeGateways;
    private long internalStaff;
    private List<RecentMerchantRequestDto> recentMerchantRequests;
}
