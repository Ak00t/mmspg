package com.ojt_22.mmspg.service;

import java.util.UUID;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;

public interface DashboardService {

	public MerchantDashboardSummaryResponse getDashboardSummary(UUID merchantId);

}
