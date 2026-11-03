package com.ojt_22.mmspg.service;

import java.time.LocalDate;
import java.util.List;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import org.springframework.web.multipart.MultipartFile;

public interface DashboardService {

    MerchantDashboardSummaryResponse getDashboardSummary(LocalDate startDate, LocalDate endDate);

    MerchantProfileResponseDto getMerchantProfile();

    List<PaymentTransactionSummaryDto> getRecentTransactions(int limit);

    String uploadProfilePicture(MultipartFile file);

    void changeMerchantPassword(ChangePasswordRequestDto request);
}
