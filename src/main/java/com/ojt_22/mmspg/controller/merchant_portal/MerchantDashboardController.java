package com.ojt_22.mmspg.controller.merchant_portal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import com.ojt_22.mmspg.service.DashboardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/merchant/dashboard")
public class MerchantDashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/summary")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<MerchantDashboardSummaryResponse> getSummary(
			@RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {
		LocalDate today = LocalDate.now();
		LocalDate resolvedStartDate = startDate != null ? startDate : today;
		LocalDate resolvedEndDate = endDate != null ? endDate : today;
		if (resolvedStartDate.isAfter(resolvedEndDate)) {
			throw new IllegalArgumentException("startDate must not be after endDate");
		}
		MerchantDashboardSummaryResponse response = dashboardService.getDashboardSummary(resolvedStartDate,
				resolvedEndDate);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/profile")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<MerchantProfileResponseDto> getProfile() {
		return ResponseEntity.ok(dashboardService.getMerchantProfile());
	}

	@PostMapping("/profile-picture")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
		try {
			String imageUrl = dashboardService.uploadProfilePicture(file);
			return ResponseEntity.ok(Map.of(
					"message", "Profile picture uploaded successfully",
					"imageUrl", "http://127.0.0.1:8004" + imageUrl
			));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@PutMapping("/profile/password")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
		try {
			dashboardService.changeMerchantPassword(request);
			return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/recent-transactions")
	@PreAuthorize("hasRole('MERCHANT')")
	public ResponseEntity<Page<PaymentTransactionSummaryDto>> getRecentTransactions(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) Integer limit) {
		int effectiveSize = (limit != null && limit > 0) ? limit : size;
		return ResponseEntity.ok(dashboardService.getRecentTransactions(page, effectiveSize));
	}
}
