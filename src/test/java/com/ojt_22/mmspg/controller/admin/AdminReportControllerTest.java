package com.ojt_22.mmspg.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ojt_22.mmspg.service.AdminExportReportService;
import com.ojt_22.mmspg.service.ExportedReport;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    @Mock
    private AdminExportReportService reportService;

    @InjectMocks
    private AdminReportController controller;

    @Test
    void testSalesLedgerExport_AllMerchants() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 8);
        byte[] content = "Merchant Code,Merchant Name\nMCH-001,Shop".getBytes(StandardCharsets.UTF_8);
        ExportedReport dummyReport = new ExportedReport(new ByteArrayResource(content), "admin-sales-ledger.csv", "text/csv; charset=UTF-8");

        when(reportService.salesLedger(eq(null), eq(startDate), eq(endDate), eq("csv"), eq("ALL")))
                .thenReturn(dummyReport);

        ResponseEntity<ByteArrayResource> response = controller.salesLedger(null, startDate, endDate, "csv", "ALL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("attachment; filename=\"admin-sales-ledger.csv\"", response.getHeaders().getFirst("Content-Disposition"));
        verify(reportService).salesLedger(null, startDate, endDate, "csv", "ALL");
    }

    @Test
    void testSalesLedgerExport_SpecificMerchant() {
        UUID merchantId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 8);
        byte[] content = "dummy-pdf-bytes".getBytes(StandardCharsets.UTF_8);
        ExportedReport dummyReport = new ExportedReport(new ByteArrayResource(content), "admin-sales-ledger.pdf", "application/pdf");

        when(reportService.salesLedger(eq(merchantId), eq(startDate), eq(endDate), eq("pdf"), eq("COMPLETED")))
                .thenReturn(dummyReport);

        ResponseEntity<ByteArrayResource> response = controller.salesLedger(merchantId, startDate, endDate, "pdf", "COMPLETED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("attachment; filename=\"admin-sales-ledger.pdf\"", response.getHeaders().getFirst("Content-Disposition"));
        verify(reportService).salesLedger(merchantId, startDate, endDate, "pdf", "COMPLETED");
    }

    @Test
    void testSettlementsExport_AllMerchants() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 8);
        byte[] content = "dummy-xlsx-bytes".getBytes(StandardCharsets.UTF_8);
        ExportedReport dummyReport = new ExportedReport(new ByteArrayResource(content), "admin-settlements.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        when(reportService.settlements(eq(null), eq(startDate), eq(endDate), eq("xlsx"), eq(null)))
                .thenReturn(dummyReport);

        ResponseEntity<ByteArrayResource> response = controller.settlements(null, startDate, endDate, "xlsx", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("attachment; filename=\"admin-settlements.xlsx\"", response.getHeaders().getFirst("Content-Disposition"));
        verify(reportService).settlements(null, startDate, endDate, "xlsx", null);
    }

    @Test
    void testSettlementsExport_SpecificMerchant() {
        UUID merchantId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 8);
        byte[] content = "Settlement Reference,Amount\nSTL-001,5000".getBytes(StandardCharsets.UTF_8);
        ExportedReport dummyReport = new ExportedReport(new ByteArrayResource(content), "admin-settlements.csv", "text/csv; charset=UTF-8");

        when(reportService.settlements(eq(merchantId), eq(startDate), eq(endDate), eq("csv"), eq("COMPLETED")))
                .thenReturn(dummyReport);

        ResponseEntity<ByteArrayResource> response = controller.settlements(merchantId, startDate, endDate, "csv", "COMPLETED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("attachment; filename=\"admin-settlements.csv\"", response.getHeaders().getFirst("Content-Disposition"));
        verify(reportService).settlements(merchantId, startDate, endDate, "csv", "COMPLETED");
    }
}
