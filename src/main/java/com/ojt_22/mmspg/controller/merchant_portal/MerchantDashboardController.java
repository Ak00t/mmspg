package com.ojt_22.mmspg.controller.merchant_portal;

import java.time.LocalDate;
<<<<<<< Updated upstream
import java.util.UUID;
=======
>>>>>>> Stashed changes

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant/dashboard")
public class MerchantDashboardController {

	private final DashboardService dashboardService;

<<<<<<< Updated upstream
	@GetMapping("/{merchantId}/summary")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<MerchantDashboardSummaryResponse> getSummary(
			@PathVariable UUID merchantId,
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate) {
		MerchantDashboardSummaryResponse response = dashboardService.getDashboardSummary(merchantId, startDate, endDate);
=======
	@GetMapping("/summary")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<MerchantDashboardSummaryResponse> getSummary(
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate) {
		LocalDate today = LocalDate.now();
		LocalDate resolvedStartDate = startDate != null ? startDate : today;
		LocalDate resolvedEndDate = endDate != null ? endDate : today;
		if (resolvedStartDate.isAfter(resolvedEndDate)) {
			throw new IllegalArgumentException("startDate must not be after endDate");
		}
		MerchantDashboardSummaryResponse response = dashboardService.getDashboardSummary(
				resolvedStartDate, resolvedEndDate);
>>>>>>> Stashed changes
		return ResponseEntity.ok(response);
	}

}
