package com.ojt_22.mmspg.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.MerchantDashboardReportRepository;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MerchantRepository merchantRepository;
    private final MerchantDashboardReportRepository dashboardReportRepository;

    @Override
    public MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication is required");
        }

        UUID merchantId = merchantRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated merchant was not found"))
                .getId();

        return dashboardReportRepository.getSummary(merchantId, resolvedStartDate, resolvedEndDate);
    }
}
