package com.ojt_22.mmspg.repository.reporting;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.dto.MerchantDashboardSummaryResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MerchantDashboardReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public MerchantDashboardSummaryResponse getSummary(
            UUID merchantId,
            LocalDate startDate,
            LocalDate endDate) {

        String sql = "CALL sp_get_merchant_dashboard_summary(UUID_TO_BIN(?), ?, ?)";

        Timestamp startTimestamp = startDate != null
                ? Timestamp.valueOf(startDate.atStartOfDay())
                : null;
        Timestamp endTimestamp = endDate != null
                ? Timestamp.valueOf(endDate.atTime(23, 59, 59, 999_999_999)
                        .withNano(999_999_000))
                : null;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNum) -> {
                    MerchantDashboardSummaryResponse response = new MerchantDashboardSummaryResponse();
                    response.setTodayGrossSales(defaultAmount(resultSet.getBigDecimal("grossSales")));
                    response.setTotalApiTransactionsToday(resultSet.getLong("totalTransactions"));
                    response.setAvailableSettlementBalance(
                            defaultAmount(resultSet.getBigDecimal("availableSettlementBalance")));
                    response.setPendingSettlementsCount(resultSet.getLong("pendingSettlementsCount"));

                    Map<String, Long> statusCounts = new LinkedHashMap<>();
                    statusCounts.put("INITIATED", resultSet.getLong("initiatedCount"));
                    statusCounts.put("PENDING_AUTHORIZATION", resultSet.getLong("pendingAuthorizationCount"));
                    statusCounts.put("COMPLETED", resultSet.getLong("completedCount"));
                    statusCounts.put("FAILED", resultSet.getLong("failedCount"));
                    response.setTransactionsByStatus(statusCounts);
                    return response;
                },
                merchantId.toString(),
                startTimestamp,
                endTimestamp);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
