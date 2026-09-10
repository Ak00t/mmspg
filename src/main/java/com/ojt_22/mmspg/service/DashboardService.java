package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor

public class DashboardService {

	private final PaymentTransactionRepository transactionRepository;
	private final SettlementRepository settlementRepository;

	public MerchantDashboardSummaryResponse getDashboardSummary(UUID merchantId) {
		LocalDateTime startOfDay = LocalDate.now()
				.atStartOfDay();
		LocalDateTime endOfDay = LocalDate.now()
				.atTime(LocalTime.MAX);
		BigDecimal todayGrossSales = transactionRepository.sumGrossSalesByDateRange(merchantId, startOfDay, endOfDay);
		Long totalTransactions = transactionRepository.countTransactionsByDateRange(merchantId, startOfDay, endOfDay);
		BigDecimal availableBalance = settlementRepository.findAvailableSettlementBalance(merchantId);

		MerchantDashboardSummaryResponse response = new MerchantDashboardSummaryResponse();
		response.setTodayGrossSales(todayGrossSales != null ? todayGrossSales : BigDecimal.ZERO);
		response.setTotalApiTransactionsToday(totalTransactions != null ? totalTransactions : 0L);
		response.setAvailableSettlementBalance(availableBalance != null ? availableBalance : BigDecimal.ZERO);

		return response;

	}
}
