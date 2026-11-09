package com.ojt_22.mmspg.service;

import java.time.LocalDate;
import java.util.List;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface DashboardService {

    MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate);

    MerchantProfileResponseDto getMerchantProfile();

    Page<PaymentTransactionSummaryDto> getRecentTransactions(int page, int size);

    default List<PaymentTransactionSummaryDto> getRecentTransactions(int limit) {
        return getRecentTransactions(0, limit).getContent();
    }

    String uploadProfilePicture(MultipartFile file);

    void changeMerchantPassword(ChangePasswordRequestDto request);
}
