package com.ojt_22.mmspg.controller.merchant_portal;

import java.time.LocalDate;
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
import com.ojt_22.mmspg.service.MerchantExportReportService;
import lombok.RequiredArgsConstructor;

@RestController @RequiredArgsConstructor @PreAuthorize("hasRole('MERCHANT')")
@RequestMapping("/api/v1/merchant/reports")
public class MerchantReportController {
    private final MerchantExportReportService reports;
    private ResponseEntity<ByteArrayResource> file(ByteArrayResource body, String name) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"").contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).contentLength(body.contentLength()).body(body);
    }
    @GetMapping("/sales-ledger/export")
    public ResponseEntity<ByteArrayResource> salesLedger(@RequestParam @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate startDate, @RequestParam @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate endDate) { return file(reports.salesLedger(startDate,endDate), "sales-ledger.csv"); }
    @GetMapping("/settlements/export")
    public ResponseEntity<ByteArrayResource> settlements(@RequestParam @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate startDate, @RequestParam @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate endDate) { return file(reports.settlements(startDate,endDate), "settlements.csv"); }
}
