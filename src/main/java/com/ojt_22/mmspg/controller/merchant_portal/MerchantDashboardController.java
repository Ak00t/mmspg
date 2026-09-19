package com.ojt_22.mmspg.controller.merchant_portal;

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

}
