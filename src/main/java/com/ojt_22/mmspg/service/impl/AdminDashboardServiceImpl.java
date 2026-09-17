package com.ojt_22.mmspg.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.dto.DashboardSummaryResponse;
import com.ojt_22.mmspg.dto.RecentMerchantRequestDto;
// 🔴 Enum များကို Import လုပ်ရန် မမေ့ပါနှင့်
import com.ojt_22.mmspg.enums.MerchantStatus;
import com.ojt_22.mmspg.enums.TerminalStatus;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;
import com.ojt_22.mmspg.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

	private final MerchantRepository merchantRepository;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final TerminalRepository terminalRepository;
	private final StaffUserRepository staffUserRepository;

	@Override
	public DashboardSummaryResponse getDashboardSummary() {
		DashboardSummaryResponse response = new DashboardSummaryResponse();

		// 1. Total Pending Approvals
		// 🔴 String "PENDING" အစား Enum MerchantStatus.PENDING ကို သုံးပါ
		long pendingCount = merchantRepository.countByStatus(MerchantStatus.PENDING);
		response.setTotalPendingApprovals(pendingCount);

		// 2. Total Volume Today
		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);
		BigDecimal volumeToday = paymentTransactionRepository.sumAmountByCreatedAtBetween(startOfDay, endOfDay)
				.orElse(BigDecimal.ZERO);
		response.setTotalVolumeToday(volumeToday);

		// 3. Active Gateways
		// 🔴 Terminal တွင် ACTIVE မရှိပါ။ TerminalStatus.ONLINE ကို သုံးပါ
		long activeTerminals = terminalRepository.countByStatus(TerminalStatus.ONLINE);
		response.setActiveGateways(activeTerminals);

		// 4. Internal Staff
		long staffCount = staffUserRepository.count();
		response.setInternalStaff(staffCount);

		// 5. Recent Merchant Requests
		List<RecentMerchantRequestDto> recentRequests = merchantRepository.findTop10ByOrderByCreatedAtDesc()
				.stream()
				.map(merchant -> {
					RecentMerchantRequestDto dto = new RecentMerchantRequestDto();
					dto.setRequestId(merchant.getId());
					dto.setMerchantName(merchant.getBusinessName());
					dto.setBusinessType(merchant.getMccCode() != null ? merchant.getMccCode().getMccName() : "N/A");
					dto.setDate(merchant.getCreatedAt());
					dto.setStatus(merchant.getStatus().name());
					return dto;
				})
				.collect(Collectors.toList());

		response.setRecentMerchantRequests(recentRequests);

		return response;
	}
}