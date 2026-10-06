package com.ojt_22.mmspg.repository.reporting;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.dto.SalesLedgerReportRow;
import com.ojt_22.mmspg.dto.SettlementReportRow;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MerchantExportReportRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<SalesLedgerReportRow> salesLedger(UUID id, LocalDate from, LocalDate to) {
        return jdbcTemplate.query("CALL sp_get_merchant_sales_ledger_report(UUID_TO_BIN(?), ?, ?)",
            (rs, n) -> { SalesLedgerReportRow r = new SalesLedgerReportRow();
                r.setLedgerId(rs.getString("ledgerId")); r.setTransactionReference(rs.getString("transactionReference"));
                r.setOrderId(rs.getString("orderId")); r.setEntryType(rs.getString("entryType"));
                r.setBalanceType(rs.getString("balanceType")); r.setAmount(rs.getBigDecimal("amount"));
                r.setDescription(rs.getString("description")); r.setTransactionStatus(rs.getString("transactionStatus"));
                r.setSettlementReference(rs.getString("settlementReference"));
                Timestamp created = rs.getTimestamp("createdAt"); if (created != null) r.setCreatedAt(created.toLocalDateTime());
                java.sql.Date settled = rs.getDate("settlementDate"); if (settled != null) r.setSettlementDate(settled.toLocalDate()); return r; },
            id.toString(), Timestamp.valueOf(from.atStartOfDay()), Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusNanos(1)));
    }

    public List<SettlementReportRow> settlements(UUID id, LocalDate from, LocalDate to) {
        return jdbcTemplate.query("CALL sp_get_merchant_settlement_report(UUID_TO_BIN(?), ?, ?)",
            (rs, n) -> { SettlementReportRow r = new SettlementReportRow();
                r.setSettlementId(rs.getString("settlementId")); r.setSettlementReference(rs.getString("settlementReference"));
                r.setTransactionReference(rs.getString("transactionReference")); r.setSettlementDate(rs.getDate("settlementDate").toLocalDate());
                r.setGrossAmount(rs.getBigDecimal("grossAmount")); r.setFeeAmount(rs.getBigDecimal("feeAmount")); r.setNetAmount(rs.getBigDecimal("netAmount"));
                r.setBankAccountNo(rs.getString("bankAccountNo")); r.setStatus(rs.getString("status"));
                Timestamp processed = rs.getTimestamp("processedAt"), created = rs.getTimestamp("createdAt");
                if (processed != null) r.setProcessedAt(processed.toLocalDateTime()); if (created != null) r.setCreatedAt(created.toLocalDateTime()); return r; },
            id.toString(), from, to);
    }
}
