package com.ojt_22.mmspg.controller.merchant_portal;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant/dashboard")
public class MerchantDashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/{merchantId}/summary")
	public ResponseEntity<MerchantDashboardSummaryResponse> getSummary(@PathVariable UUID merchantId) {
		MerchantDashboardSummaryResponse response = dashboardService.getDashboardSummary(merchantId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{merchantId}/today-gross-sales")
	public ResponseEntity<BigDecimal> getTodayGrossSales(@PathVariable UUID merchantId) {
		BigDecimal todayGrossSales = dashboardService.getTodayGrossSales(merchantId);
		return ResponseEntity.ok(todayGrossSales);
	}

	@GetMapping("/{merchantId}/total-api-transactions")
	public ResponseEntity<Long> getTotalApiTransactions(@PathVariable UUID merchantId) {
		Long totalTransactions = dashboardService.getTotalApiTransactions(merchantId);
		return ResponseEntity.ok(totalTransactions);
	}

	@GetMapping("/{merchantId}/available-balance")
	public ResponseEntity<BigDecimal> getAvailableSettlementBalance(@PathVariable UUID merchantId) {
		BigDecimal availableBalance = dashboardService.getAvailableSettlementBalance(merchantId);
		return ResponseEntity.ok(availableBalance);
	}
}
