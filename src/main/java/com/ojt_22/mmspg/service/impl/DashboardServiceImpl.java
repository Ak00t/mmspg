package com.ojt_22.mmspg.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }

        UUID merchantId = getAuthenticatedMerchant().getId();

        MerchantDashboardSummaryResponse response = dashboardReportRepository.getSummary(merchantId, resolvedStartDate, resolvedEndDate);
        LocalDateTime startDateTime = resolvedStartDate.atStartOfDay();
        LocalDateTime endDateTime = resolvedEndDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Object[]> branchResults = paymentTransactionRepository.getBranchSalesSummary(merchantId, startDateTime, endDateTime);
        List<com.ojt_22.mmspg.dto.BranchSalesSummaryDto> branchSales = branchResults.stream().map(row -> com.ojt_22.mmspg.dto.BranchSalesSummaryDto.builder()
                .branchId((UUID) row[0])
                .branchName((String) row[1])
                .branchCode((String) row[2])
                .transactionCount(((Number) row[3]).longValue())
                .totalVolume((java.math.BigDecimal) row[4])
                .build()).toList();
        response.setBranchSales(branchSales);

        return response;
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
                .profileImageUrl(merchant.getProfileImageUrl())
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
                        .branchName(t.getBranch() != null ? t.getBranch().getBranchName() : null)
                        .branchCode(t.getBranch() != null ? t.getBranch().getBranchCode() : null)
                        .terminalCode(t.getTerminal() != null ? t.getTerminal().getTerminalCode() : null)
                        .terminalType(t.getTerminal() != null && t.getTerminal().getTerminalType() != null ? t.getTerminal().getTerminalType().name() : null)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public String uploadProfilePicture(MultipartFile file) {
        Merchant merchant = getAuthenticatedMerchant();
        try {
            String uploadDir = "uploads/profiles/";
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;

            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + newFilename);
            java.nio.file.Files.copy(
                    file.getInputStream(),
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            String fileUrl = "/uploads/profiles/" + newFilename;
            merchant.setProfileImageUrl(fileUrl);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);

            return fileUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void changeMerchantPassword(ChangePasswordRequestDto request) {
        Merchant merchant = getAuthenticatedMerchant();
        if (!passwordEncoder.matches(request.getCurrentPassword(), merchant.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), merchant.getPasswordHash())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password");
        }
        merchant.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);
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
