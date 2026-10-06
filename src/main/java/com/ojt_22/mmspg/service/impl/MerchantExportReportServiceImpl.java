package com.ojt_22.mmspg.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.io.InputStream;
import java.util.List;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.MerchantExportReportRepository;
import com.ojt_22.mmspg.service.MerchantExportReportService;
import com.ojt_22.mmspg.service.ExportedReport;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class MerchantExportReportServiceImpl implements MerchantExportReportService {
    private final MerchantRepository merchants;
    private final MerchantExportReportRepository reports;

    private java.util.UUID merchantId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) throw new AccessDeniedException("Authentication is required");
        return merchants.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("Merchant account was not found")).getId();
    }
    private void validate(LocalDate from, LocalDate to) { if (from == null || to == null || from.isAfter(to)) throw new IllegalArgumentException("Invalid report date range"); }
    private String csv(String... values) { return java.util.Arrays.stream(values).map(v -> { String s = v == null ? "" : v; return "\"" + s.replace("\"", "\"\"") + "\""; }).collect(java.util.stream.Collectors.joining(",")); }

    public ByteArrayResource salesLedger(LocalDate from, LocalDate to) {
        validate(from, to); var out = new StringBuilder(csv("Ledger ID","Transaction Reference","Order ID","Entry Type","Balance Type","Amount","Description","Transaction Status","Settlement Reference","Created At","Settlement Date")).append('\n');
        reports.salesLedger(merchantId(), from, to).forEach(r -> out.append(csv(r.getLedgerId(),r.getTransactionReference(),r.getOrderId(),r.getEntryType(),r.getBalanceType(),String.valueOf(r.getAmount()),r.getDescription(),r.getTransactionStatus(),r.getSettlementReference(),String.valueOf(r.getCreatedAt()),String.valueOf(r.getSettlementDate()))).append('\n'));
        return new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8));
    }
    public ByteArrayResource settlements(LocalDate from, LocalDate to) {
        validate(from, to); var out = new StringBuilder(csv("Settlement ID","Settlement Reference","Transaction Reference","Settlement Date","Gross Amount","Fee Amount","Net Amount","Bank Account","Status","Processed At","Created At")).append('\n');
        reports.settlements(merchantId(), from, to).forEach(r -> out.append(csv(r.getSettlementId(),r.getSettlementReference(),r.getTransactionReference(),String.valueOf(r.getSettlementDate()),String.valueOf(r.getGrossAmount()),String.valueOf(r.getFeeAmount()),String.valueOf(r.getNetAmount()),r.getBankAccountNo(),r.getStatus(),String.valueOf(r.getProcessedAt()),String.valueOf(r.getCreatedAt()))).append('\n'));
        return new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8));
    }

    private ExportedReport jasper(String template, List<?> rows, String baseName, String format) {
        try (InputStream input = getClass().getResourceAsStream("/reports/" + template)) {
            if (input == null) throw new IllegalStateException("Report template not found: " + template);
            JasperReport report = JasperCompileManager.compileReport(input);
            JasperPrint print = JasperFillManager.fillReport(report, java.util.Map.of(), new JRBeanCollectionDataSource(rows));
            byte[] bytes;
            String type;
            String extension;
            if ("pdf".equalsIgnoreCase(format)) {
                bytes = JasperExportManager.exportReportToPdf(print);
                type = "application/pdf"; extension = "pdf";
            } else if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                JRXlsxExporter exporter = new JRXlsxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
                SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
                config.setOnePagePerSheet(false); config.setDetectCellType(true); config.setCollapseRowSpan(false);
                exporter.setConfiguration(config); exporter.exportReport();
                bytes = output.toByteArray(); type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; extension = "xlsx";
            } else { throw new IllegalArgumentException("format must be csv, pdf, or xlsx"); }
            return new ExportedReport(new ByteArrayResource(bytes), baseName + "." + extension, type);
        } catch (Exception ex) { throw new IllegalStateException("Could not generate " + format + " report", ex); }
    }

    @Override
    public ExportedReport salesLedger(LocalDate from, LocalDate to, String format) {
        validate(from, to); String normalized = format == null ? "csv" : format.toLowerCase();
        if ("csv".equals(normalized)) return new ExportedReport(salesLedger(from, to), "sales-ledger.csv", "text/csv; charset=UTF-8");
        return jasper("sales-ledger.jrxml", reports.salesLedger(merchantId(), from, to), "sales-ledger", normalized);
    }

    @Override
    public ExportedReport settlements(LocalDate from, LocalDate to, String format) {
        validate(from, to); String normalized = format == null ? "csv" : format.toLowerCase();
        if ("csv".equals(normalized)) return new ExportedReport(settlements(from, to), "settlements.csv", "text/csv; charset=UTF-8");
        return jasper("settlement-report.jrxml", reports.settlements(merchantId(), from, to), "settlements", normalized);
    }
}
