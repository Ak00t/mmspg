package com.ojt_22.mmspg.service.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.dto.SalesLedgerReportRow;
import com.ojt_22.mmspg.dto.SettlementReportRow;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.AdminExportReportRepository;
import com.ojt_22.mmspg.service.AdminExportReportService;
import com.ojt_22.mmspg.service.ExportedReport;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Service
@RequiredArgsConstructor
public class AdminExportReportServiceImpl implements AdminExportReportService {
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final java.time.format.DateTimeFormatter DATETIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final MerchantRepository merchants;
    private final AdminExportReportRepository reports;

    private void validate(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid report date range");
        }
    }

    private String csv(String... values) {
        return java.util.Arrays.stream(values).map(v -> {
            String s = v == null ? "" : v;
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }).collect(java.util.stream.Collectors.joining(","));
    }

    private ExportedReport jasper(String template, List<?> rows, String scopeName, String scopeEmail, String scopePhone, LocalDate from, LocalDate to, String status, String baseName, String format) {
        try (InputStream input = getClass().getResourceAsStream("/reports/" + template)) {
            if (input == null) throw new IllegalStateException("Report template not found: " + template);
            JasperReport report = JasperCompileManager.compileReport(input);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("merchantBusinessName", scopeName);
            parameters.put("merchantEmail", scopeEmail);
            parameters.put("merchantPhone", scopePhone);
            parameters.put("reportPeriod", from.format(DATE_FORMATTER) + " to " + to.format(DATE_FORMATTER));
            parameters.put("statusFilter", (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) ? "ALL" : status.trim().toUpperCase());
            parameters.put("generatedAt", java.time.LocalDateTime.now().format(DATETIME_FORMATTER));
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JRBeanCollectionDataSource(rows));
            byte[] bytes;
            String type;
            String extension;
            if ("pdf".equalsIgnoreCase(format)) {
                bytes = JasperExportManager.exportReportToPdf(print);
                type = "application/pdf";
                extension = "pdf";
            } else if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
                SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
                config.setOnePagePerSheet(false);
                config.setDetectCellType(true);
                config.setCollapseRowSpan(false);
                exporter.setConfiguration(config);
                exporter.exportReport();
                bytes = output.toByteArray();
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension = "xlsx";
            } else {
                throw new IllegalArgumentException("format must be csv, pdf, or xlsx");
            }
            return new ExportedReport(new ByteArrayResource(bytes), baseName + "." + extension, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate " + format + " report", ex);
        }
    }

    @Override
    public ExportedReport salesLedger(UUID merchantId, LocalDate from, LocalDate to, String format, String status) {
        validate(from, to);
        String normalized = format == null ? "csv" : format.toLowerCase();
        String scopeName = "ALL MERCHANTS";
        String scopeEmail = "Platform: System-Wide";
        String scopePhone = "All Merchants";

        if (merchantId != null) {
            Optional<Merchant> mOpt = merchants.findById(merchantId);
            if (mOpt.isPresent()) {
                Merchant m = mOpt.get();
                scopeName = (m.getMerchantCode() != null ? "[" + m.getMerchantCode() + "] " : "") + m.getBusinessName();
                scopeEmail = "Email: " + (m.getEmail() != null ? m.getEmail() : "-");
                scopePhone = "Phone: " + (m.getPhone() != null ? m.getPhone() : "-");
            }
        }

        List<SalesLedgerReportRow> rows = reports.salesLedger(merchantId, from, to, status);

        if ("csv".equals(normalized)) {
            var out = new StringBuilder("\uFEFF");
            out.append(csv(
                    "Merchant Code", "Merchant Name", "Merchant Email", "Merchant Phone", "Branch Name", "Branch Code", "Terminal Code", "Terminal Type",
                    "Ledger ID", "Transaction Reference", "Order ID", "Entry Type",
                    "Balance Type", "Amount", "Description", "Transaction Status", "Settlement Reference",
                    "Created At", "Settlement Date"
            )).append('\n');
            rows.forEach(r -> out.append(csv(
                    r.getMerchantCode(),
                    r.getMerchantBusinessName(),
                    r.getMerchantEmail(),
                    r.getMerchantPhone(),
                    r.getBranchName(),
                    r.getBranchCode(),
                    r.getTerminalCode(),
                    r.getTerminalType(),
                    r.getLedgerId(),
                    r.getTransactionReference(),
                    r.getOrderId(),
                    r.getEntryType(),
                    r.getBalanceType(),
                    String.valueOf(r.getAmount()),
                    r.getDescription(),
                    r.getTransactionStatus(),
                    r.getSettlementReference(),
                    r.getFormattedCreatedAt(),
                    r.getFormattedSettlementDate()
            )).append('\n'));
            return new ExportedReport(new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8)), "admin-sales-ledger.csv", "text/csv; charset=UTF-8");
        }
        return jasper("admin-sales-ledger.jrxml", rows, scopeName, scopeEmail, scopePhone, from, to, status, "admin-sales-ledger", normalized);
    }

    @Override
    public ExportedReport settlements(UUID merchantId, LocalDate from, LocalDate to, String format, String status) {
        validate(from, to);
        String normalized = format == null ? "csv" : format.toLowerCase();
        String scopeName = "ALL MERCHANTS";
        String scopeEmail = "Platform: System-Wide";
        String scopePhone = "All Merchants";

        if (merchantId != null) {
            Optional<Merchant> mOpt = merchants.findById(merchantId);
            if (mOpt.isPresent()) {
                Merchant m = mOpt.get();
                scopeName = (m.getMerchantCode() != null ? "[" + m.getMerchantCode() + "] " : "") + m.getBusinessName();
                scopeEmail = "Email: " + (m.getEmail() != null ? m.getEmail() : "-");
                scopePhone = "Phone: " + (m.getPhone() != null ? m.getPhone() : "-");
            }
        }

        List<SettlementReportRow> rows = reports.settlements(merchantId, from, to, status);

        if ("csv".equals(normalized)) {
            var out = new StringBuilder("\uFEFF");
            out.append(csv(
                    "Merchant Code", "Merchant Name", "Merchant Email", "Merchant Phone", "Branch Name", "Branch Code", "Terminal Code", "Terminal Type",
                    "Settlement ID", "Settlement Reference", "Transaction Reference",
                    "Settlement Date", "Gross Amount", "Fee Amount", "Net Amount",
                    "Bank Account", "Status", "Processed At", "Created At"
            )).append('\n');
            rows.forEach(r -> out.append(csv(
                    r.getMerchantCode(),
                    r.getMerchantBusinessName(),
                    r.getMerchantEmail(),
                    r.getMerchantPhone(),
                    r.getBranchName(),
                    r.getBranchCode(),
                    r.getTerminalCode(),
                    r.getTerminalType(),
                    r.getSettlementId(),
                    r.getSettlementReference(),
                    r.getTransactionReference(),
                    r.getFormattedSettlementDate(),
                    String.valueOf(r.getGrossAmount()),
                    String.valueOf(r.getFeeAmount()),
                    String.valueOf(r.getNetAmount()),
                    r.getBankAccountNo(),
                    r.getStatus(),
                    r.getFormattedProcessedAt(),
                    r.getFormattedCreatedAt()
            )).append('\n'));
            return new ExportedReport(new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8)), "admin-settlements.csv", "text/csv; charset=UTF-8");
        }
        return jasper("admin-settlement-report.jrxml", rows, scopeName, scopeEmail, scopePhone, from, to, status, "admin-settlements", normalized);
    }
}
