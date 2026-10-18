package com.ojt_22.mmspg.service.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.dto.SalesLedgerReportRow;
import com.ojt_22.mmspg.dto.SettlementReportRow;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.MerchantExportReportRepository;
import com.ojt_22.mmspg.service.ExportedReport;
import com.ojt_22.mmspg.service.MerchantExportReportService;

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
public class MerchantExportReportServiceImpl implements MerchantExportReportService {
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final java.time.format.DateTimeFormatter DATETIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final MerchantRepository merchants;
    private final MerchantExportReportRepository reports;

    private Merchant currentMerchant() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Authentication is required");
        }
        return merchants.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Merchant account was not found"));
    }

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

    @Override
    public ByteArrayResource salesLedger(LocalDate from, LocalDate to) {
        return (ByteArrayResource) salesLedger(from, to, "csv", null).resource();
    }

    @Override
    public ByteArrayResource settlements(LocalDate from, LocalDate to) {
        return (ByteArrayResource) settlements(from, to, "csv", null).resource();
    }

    @Override
    public ExportedReport salesLedger(LocalDate from, LocalDate to, String format) {
        return salesLedger(from, to, format, null);
    }

    @Override
    public ExportedReport settlements(LocalDate from, LocalDate to, String format) {
        return settlements(from, to, format, null);
    }

    private ExportedReport jasper(String template, List<?> rows, Merchant merchant, LocalDate from, LocalDate to, String status, String baseName, String format) {
        try (InputStream input = getClass().getResourceAsStream("/reports/" + template)) {
            if (input == null) throw new IllegalStateException("Report template not found: " + template);
            JasperReport report = JasperCompileManager.compileReport(input);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("merchantBusinessName", merchant.getBusinessName());
            parameters.put("merchantEmail", merchant.getEmail());
            parameters.put("merchantPhone", merchant.getPhone());
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
    public ExportedReport salesLedger(LocalDate from, LocalDate to, String format, String status) {
        validate(from, to);
        String normalized = format == null ? "csv" : format.toLowerCase();
        Merchant merchant = currentMerchant();
        List<SalesLedgerReportRow> rows = reports.salesLedger(merchant.getId(), from, to, status);
        rows.forEach(r -> {
            r.setMerchantBusinessName(merchant.getBusinessName());
            r.setMerchantEmail(merchant.getEmail());
            r.setMerchantPhone(merchant.getPhone());
        });

        if ("csv".equals(normalized)) {
            var out = new StringBuilder(csv(
                    "Merchant Name", "Merchant Email", "Merchant Phone", "Ledger ID", "Transaction Reference", "Order ID", "Entry Type",
                    "Balance Type", "Amount", "Description", "Transaction Status", "Settlement Reference",
                    "Created At", "Settlement Date"
            )).append('\n');
            rows.forEach(r -> out.append(csv(
                    merchant.getBusinessName(),
                    merchant.getEmail(),
                    merchant.getPhone(),
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
            return new ExportedReport(new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8)), "sales-ledger.csv", "text/csv; charset=UTF-8");
        }
        return jasper("sales-ledger.jrxml", rows, merchant, from, to, status, "sales-ledger", normalized);
    }

    @Override
    public ExportedReport settlements(LocalDate from, LocalDate to, String format, String status) {
        validate(from, to);
        String normalized = format == null ? "csv" : format.toLowerCase();
        Merchant merchant = currentMerchant();
        List<SettlementReportRow> rows = reports.settlements(merchant.getId(), from, to, status);
        rows.forEach(r -> {
            r.setMerchantBusinessName(merchant.getBusinessName());
            r.setMerchantEmail(merchant.getEmail());
            r.setMerchantPhone(merchant.getPhone());
        });

        if ("csv".equals(normalized)) {
            var out = new StringBuilder(csv(
                    "Merchant Name", "Merchant Email", "Merchant Phone", "Settlement ID", "Settlement Reference", "Transaction Reference",
                    "Settlement Date", "Gross Amount", "Fee Amount", "Net Amount",
                    "Bank Account", "Status", "Processed At", "Created At"
            )).append('\n');
            rows.forEach(r -> out.append(csv(
                    merchant.getBusinessName(),
                    merchant.getEmail(),
                    merchant.getPhone(),
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
            return new ExportedReport(new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8)), "settlements.csv", "text/csv; charset=UTF-8");
        }
        return jasper("settlement-report.jrxml", rows, merchant, from, to, status, "settlements", normalized);
    }
}
