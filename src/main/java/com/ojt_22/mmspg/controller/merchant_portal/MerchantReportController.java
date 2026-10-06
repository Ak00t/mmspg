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

import com.ojt_22.mmspg.service.ExportedReport;
import com.ojt_22.mmspg.service.MerchantExportReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@RequestMapping("/api/v1/merchant/reports")
public class MerchantReportController {
	private final MerchantExportReportService reports;

	private ResponseEntity<ByteArrayResource> file(ExportedReport report) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.filename() + "\"")
				.contentType(MediaType.parseMediaType(report.contentType()))
				.contentLength(report.resource()
						.contentLength())
				.body(report.resource());
	}

	@GetMapping("/sales-ledger/export")
	public ResponseEntity<ByteArrayResource> salesLedger(
			@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
			@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
			@RequestParam(defaultValue = "csv") String format) {
		return file(reports.salesLedger(startDate, endDate, format));
	}

	@GetMapping("/settlements/export")
	public ResponseEntity<ByteArrayResource> settlements(
			@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
			@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate,
			@RequestParam(defaultValue = "csv") String format) {
		return file(reports.settlements(startDate, endDate, format));
	}
}
