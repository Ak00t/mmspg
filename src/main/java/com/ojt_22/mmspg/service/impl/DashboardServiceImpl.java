package com.ojt_22.mmspg.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.repository.reporting.MerchantDashboardReportRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final MerchantDashboardReportRepository dashboardReportRepository;
	private final MerchantRepository merchantRepository;

	public MerchantDashboardSummaryResponse getDashboardSummary(UUID merchantId, LocalDate startDate, LocalDate endDate) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("Authentication is required");
		}

		UUID authenticatedMerchantId = merchantRepository.findByEmail(authentication.getName())
				.map(merchant -> merchant.getId())
				.orElseThrow(() -> new AccessDeniedException("Authenticated merchant was not found"));

		if (!authenticatedMerchantId.equals(merchantId)) {
			throw new AccessDeniedException("You cannot access another merchant's dashboard");
		}

		if (startDate == null) {
			startDate = LocalDate.now();
		}
		if (endDate == null) {
			endDate = LocalDate.now();
		}
		
		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("startDate must not be after endDate");
		}

		return dashboardReportRepository.getSummary(merchantId, startDate, endDate);
	}

}
