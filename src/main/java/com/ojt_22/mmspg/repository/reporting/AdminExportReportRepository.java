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
public class AdminExportReportRepository {
    private final JdbcTemplate jdbcTemplate;

    private String normalizeStatus(String status) {
        return (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) ? null : status.trim();
    }

    public List<SalesLedgerReportRow> salesLedger(UUID merchantId, LocalDate from, LocalDate to, String status) {
        String merchantIdStr = merchantId != null ? merchantId.toString() : null;
        String sql = merchantId != null
                ? "CALL sp_get_admin_sales_ledger_report(UUID_TO_BIN(?), ?, ?, ?)"
                : "CALL sp_get_admin_sales_ledger_report(NULL, ?, ?, ?)";

        Object[] params = merchantId != null
                ? new Object[]{merchantIdStr, Timestamp.valueOf(from.atStartOfDay()), Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusNanos(1)), normalizeStatus(status)}
                : new Object[]{Timestamp.valueOf(from.atStartOfDay()), Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusNanos(1)), normalizeStatus(status)};

        return jdbcTemplate.query(sql, (rs, n) -> {
            SalesLedgerReportRow r = new SalesLedgerReportRow();
            r.setLedgerId(rs.getString("ledgerId"));
            r.setMerchantCode(rs.getString("merchantCode"));
            r.setMerchantBusinessName(rs.getString("merchantBusinessName"));
            r.setMerchantEmail(rs.getString("merchantEmail"));
            r.setMerchantPhone(rs.getString("merchantPhone"));
            r.setTransactionReference(rs.getString("transactionReference"));
            r.setOrderId(rs.getString("orderId"));
            r.setEntryType(rs.getString("entryType"));
            r.setBalanceType(rs.getString("balanceType"));
            r.setAmount(rs.getBigDecimal("amount"));
            r.setDescription(rs.getString("description"));
            r.setTransactionStatus(rs.getString("transactionStatus"));
            r.setSettlementReference(rs.getString("settlementReference"));
            r.setBranchName(rs.getString("branchName"));
            r.setBranchCode(rs.getString("branchCode"));
            r.setTerminalCode(rs.getString("terminalCode"));
            r.setTerminalType(rs.getString("terminalType"));
            Timestamp created = rs.getTimestamp("createdAt");
            if (created != null) r.setCreatedAt(created.toLocalDateTime());
            java.sql.Date settled = rs.getDate("settlementDate");
            if (settled != null) r.setSettlementDate(settled.toLocalDate());
            return r;
        }, params);
    }

    public List<SettlementReportRow> settlements(UUID merchantId, LocalDate from, LocalDate to, String status) {
        String merchantIdStr = merchantId != null ? merchantId.toString() : null;
        String sql = merchantId != null
                ? "CALL sp_get_admin_settlement_report(UUID_TO_BIN(?), ?, ?, ?)"
                : "CALL sp_get_admin_settlement_report(NULL, ?, ?, ?)";

        Object[] params = merchantId != null
                ? new Object[]{merchantIdStr, from, to, normalizeStatus(status)}
                : new Object[]{from, to, normalizeStatus(status)};

        return jdbcTemplate.query(sql, (rs, n) -> {
            SettlementReportRow r = new SettlementReportRow();
            r.setSettlementId(rs.getString("settlementId"));
            r.setMerchantCode(rs.getString("merchantCode"));
            r.setMerchantBusinessName(rs.getString("merchantBusinessName"));
            r.setMerchantEmail(rs.getString("merchantEmail"));
            r.setMerchantPhone(rs.getString("merchantPhone"));
            r.setBranchName(rs.getString("branchName"));
            r.setBranchCode(rs.getString("branchCode"));
            r.setTerminalCode(rs.getString("terminalCode"));
            r.setTerminalType(rs.getString("terminalType"));
            r.setSettlementReference(rs.getString("settlementReference"));
            r.setTransactionReference(rs.getString("transactionReference"));
            java.sql.Date date = rs.getDate("settlementDate");
            if (date != null) r.setSettlementDate(date.toLocalDate());
            r.setGrossAmount(rs.getBigDecimal("grossAmount"));
            r.setFeeAmount(rs.getBigDecimal("feeAmount"));
            r.setNetAmount(rs.getBigDecimal("netAmount"));
            r.setBankAccountNo(rs.getString("bankAccountNo"));
            r.setStatus(rs.getString("status"));
            Timestamp processed = rs.getTimestamp("processedAt");
            Timestamp created = rs.getTimestamp("createdAt");
            if (processed != null) r.setProcessedAt(processed.toLocalDateTime());
            if (created != null) r.setCreatedAt(created.toLocalDateTime());
            return r;
        }, params);
    }
}
