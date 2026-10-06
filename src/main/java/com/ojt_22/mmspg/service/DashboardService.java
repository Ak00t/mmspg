package com.ojt_22.mmspg.service;

import java.time.LocalDate;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;

public interface DashboardService {

    MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate);
}
