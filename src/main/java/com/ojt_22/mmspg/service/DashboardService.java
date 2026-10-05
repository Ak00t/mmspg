package com.ojt_22.mmspg.service;

import java.time.LocalDate;
<<<<<<< Updated upstream
import java.util.UUID;
=======
>>>>>>> Stashed changes

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;

public interface DashboardService {

<<<<<<< Updated upstream
	public MerchantDashboardSummaryResponse getDashboardSummary(UUID merchantId, LocalDate startDate, LocalDate endDate);
=======
	MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate);
>>>>>>> Stashed changes

}
