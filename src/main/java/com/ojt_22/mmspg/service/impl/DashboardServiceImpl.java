package com.ojt_22.mmspg.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.enums.SettlementStatus;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.SettlementRepository;
import com.ojt_22.mmspg.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final PaymentTransactionRepository transactionRepository;
	private final SettlementRepository settlementRepository;

	public MerchantDashboardSummaryResponse getDashboardSummary(UUID merchantId, LocalDate startDate, LocalDate endDate) {
		if (startDate == null) {
			startDate = LocalDate.now();
		}
		if (endDate == null) {
			endDate = LocalDate.now();
		}
		
		LocalDateTime startOfDay = startDate.atStartOfDay();
		LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

		BigDecimal grossSales = transactionRepository.sumGrossSalesByDateRange(merchantId, startOfDay, endOfDay);
		Long totalTransactions = transactionRepository.countTransactionsByDateRange(merchantId, startOfDay, endOfDay);
		BigDecimal availableBalance = settlementRepository.findAvailableSettlementBalance(merchantId);

		List<Object[]> statusCounts = transactionRepository.countGroupedByStatus(merchantId, startOfDay, endOfDay);
		Map<String, Long> transactionsByStatus = statusCounts.stream()
				.collect(Collectors.toMap(
						obj -> obj[0].toString(),
						obj -> (Long) obj[1]
				));
				
		Long pendingSettlementsCount = settlementRepository.countByMerchantIdAndStatus(merchantId, SettlementStatus.PENDING);

		MerchantDashboardSummaryResponse response = new MerchantDashboardSummaryResponse();
		response.setTodayGrossSales(grossSales != null ? grossSales : BigDecimal.ZERO);
		response.setTotalApiTransactionsToday(totalTransactions != null ? totalTransactions : 0L);
		response.setAvailableSettlementBalance(availableBalance != null ? availableBalance : BigDecimal.ZERO);
		response.setTransactionsByStatus(transactionsByStatus);
		response.setPendingSettlementsCount(pendingSettlementsCount != null ? pendingSettlementsCount : 0L);
		
		return response;
	}

}
