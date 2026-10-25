package com.ojt_22.mmspg.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.reporting.MerchantDashboardReportRepository;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MerchantRepository merchantRepository;
    private final MerchantDashboardReportRepository dashboardReportRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }

        UUID merchantId = getAuthenticatedMerchant().getId();

        return dashboardReportRepository.getSummary(merchantId, resolvedStartDate, resolvedEndDate);
    }

    @Override
    public MerchantProfileResponseDto getMerchantProfile() {
        Merchant merchant = getAuthenticatedMerchant();

        return MerchantProfileResponseDto.builder()
                .merchantId(merchant.getId())
                .merchantCode(merchant.getMerchantCode())
                .businessName(merchant.getBusinessName())
                .businessRegistrationNo(merchant.getBusinessRegistrationNo())
                .email(merchant.getEmail())
                .phone(merchant.getPhone())
                .contactName(merchant.getContactName())
                .settlementAccountNo(merchant.getSettlementAccountNo())
                .status(merchant.getStatus() != null ? merchant.getStatus().name() : null)
                .mccCode(merchant.getMccCode() != null ? merchant.getMccCode().getMccCode() : null)
                .mccDescription(merchant.getMccCode() != null ? merchant.getMccCode().getMccName() : null)
                .build();
    }

    @Override
    public List<PaymentTransactionSummaryDto> getRecentTransactions(int limit) {
        UUID merchantId = getAuthenticatedMerchant().getId();

        List<PaymentTransaction> transactions = paymentTransactionRepository.findTop10ByMerchantIdOrderByCreatedAtDesc(merchantId);

        return transactions.stream()
                .map(t -> PaymentTransactionSummaryDto.builder()
                        .transactionReference(t.getTransactionReference())
                        .orderId(t.getOrderId())
                        .amount(t.getAmount())
                        .currency(t.getCurrency())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .initiatedAt(t.getInitiatedAt() != null ? t.getInitiatedAt() : t.getCreatedAt())
                        .build())
                .toList();
    }

    private Merchant getAuthenticatedMerchant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication is required");
        }

        return merchantRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated merchant was not found"));
    }
}
