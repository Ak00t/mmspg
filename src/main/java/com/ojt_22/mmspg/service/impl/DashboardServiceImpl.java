package com.ojt_22.mmspg.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
<<<<<<< Updated upstream
import com.ojt_22.mmspg.repository.reporting.MerchantDashboardReportRepository;
=======
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.SettlementRepository;
>>>>>>> Stashed changes
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

<<<<<<< Updated upstream
	private final MerchantDashboardReportRepository dashboardReportRepository;
	private final MerchantRepository merchantRepository;

	public MerchantDashboardSummaryResponse getDashboardSummary(UUID merchantId, LocalDate startDate, LocalDate endDate) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("Authentication is required");
		}
=======
	private final PaymentTransactionRepository transactionRepository;
	private final SettlementRepository settlementRepository;
	private final MerchantRepository merchantRepository;

	public MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new org.springframework.security.access.AccessDeniedException("Authentication is required");
		}
		UUID merchantId = merchantRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Merchant account not found"))
				.getId();
		LocalDateTime startOfDay = startDate
				.atStartOfDay();
		LocalDateTime endOfDay = endDate
				.atTime(LocalTime.MAX);
>>>>>>> Stashed changes

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
