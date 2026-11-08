package com.ojt_22.mmspg.controller.admin;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.service.AdminExportReportService;
import com.ojt_22.mmspg.service.ExportedReport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'SUPPORT')")
@RequestMapping("/api/v1/admin/reports")
@Tag(name = "Admin Reporting", description = "Endpoints for Admin, Auditor, and Support Report Exports")
public class AdminReportController {
    private final AdminExportReportService reports;

    private ResponseEntity<ByteArrayResource> file(ExportedReport report) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.filename() + "\"")
                .contentType(MediaType.parseMediaType(report.contentType()))
                .contentLength(report.resource().contentLength())
                .body(report.resource());
    }

    @GetMapping("/sales-ledger/export")
    @Operation(summary = "Export Sales Ledger Report", description = "Exports Sales Ledger records for all merchants or a specific merchant in CSV, XLSX, or PDF.")
    public ResponseEntity<ByteArrayResource> salesLedger(
            @RequestParam(required = false) UUID merchantId,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String status) {
        return file(reports.salesLedger(merchantId, startDate, endDate, format, status));
    }

    @GetMapping("/settlements/export")
    @Operation(summary = "Export Settlement Report", description = "Exports Settlement payout records for all merchants or a specific merchant in CSV, XLSX, or PDF.")
    public ResponseEntity<ByteArrayResource> settlements(
            @RequestParam(required = false) UUID merchantId,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String status) {
        return file(reports.settlements(merchantId, startDate, endDate, format, status));
    }
}
