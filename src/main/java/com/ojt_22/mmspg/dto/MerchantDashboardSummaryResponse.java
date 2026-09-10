package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantDashboardSummaryResponse {
	private BigDecimal todayGrossSales;
	private Long totalApiTransactionsToday;
	private BigDecimal availableSettlementBalance;
	private Long pendingSettlementsCount;
	private Map<String, Long> transactionsByStatus;

}
