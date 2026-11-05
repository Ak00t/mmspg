package com.ojt_22.mmspg.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDashboardSummaryDto {
    private long totalEvents;
    private long successCount;
    private long failedCount;
    private double successRate;
    private long todayEvents;
    private long activeActorsCount;
    private Map<String, Long> actionBreakdown;
    private Map<String, Long> menuBreakdown;
    private List<AuditLogResponseDto> recentCriticalEvents;
}
