package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ojt_22.mmspg.dto.SalesLedgerReportRow;
import com.ojt_22.mmspg.dto.SettlementReportRow;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.AdminExportReportRepository;
import com.ojt_22.mmspg.service.ExportedReport;

@ExtendWith(MockitoExtension.class)
class AdminExportReportServiceImplTest {

    @Mock
    private AdminExportReportRepository reports;

    @Mock
    private MerchantRepository merchants;

    @InjectMocks
    private AdminExportReportServiceImpl service;

    private SalesLedgerReportRow salesRow;
    private SettlementReportRow settlementRow;

    @BeforeEach
    void setUp() {
        salesRow = new SalesLedgerReportRow();
        salesRow.setMerchantCode("MC-000001");
        salesRow.setMerchantBusinessName("Ocean Supercenter");
        salesRow.setMerchantEmail("ocean@gmail.com");
        salesRow.setMerchantPhone("09987654321");
        salesRow.setBranchName("Ocean Hledan Branch");
        salesRow.setBranchCode("BR-MC-000001-01");
        salesRow.setTerminalCode("POS-000001");
        salesRow.setTerminalType("PHYSICAL_POS");
        salesRow.setLedgerId(UUID.randomUUID().toString());
        salesRow.setTransactionReference("TXN-001");
        salesRow.setOrderId("ORD-001");
        salesRow.setEntryType("CREDIT");
        salesRow.setBalanceType("SETTLED");
        salesRow.setAmount(new BigDecimal("50000.00"));
        salesRow.setDescription("Test order");
        salesRow.setTransactionStatus("COMPLETED");
        salesRow.setSettlementReference("STL-001");
        salesRow.setCreatedAt(LocalDateTime.now());
        salesRow.setSettlementDate(LocalDate.now());

        settlementRow = new SettlementReportRow();
        settlementRow.setMerchantCode("MC-000001");
        settlementRow.setMerchantBusinessName("Ocean Supercenter");
        settlementRow.setMerchantEmail("ocean@gmail.com");
        settlementRow.setMerchantPhone("09987654321");
        settlementRow.setBranchName("Ocean Hledan Branch");
        settlementRow.setBranchCode("BR-MC-000001-01");
        settlementRow.setTerminalCode("POS-000001");
        settlementRow.setTerminalType("PHYSICAL_POS");
        settlementRow.setSettlementId(UUID.randomUUID().toString());
        settlementRow.setSettlementReference("STL-001");
        settlementRow.setTransactionReference("TXN-001");
        settlementRow.setSettlementDate(LocalDate.now());
        settlementRow.setGrossAmount(new BigDecimal("50000.00"));
        settlementRow.setFeeAmount(new BigDecimal("500.00"));
        settlementRow.setNetAmount(new BigDecimal("49500.00"));
        settlementRow.setBankAccountNo("2001122334");
        settlementRow.setStatus("COMPLETED");
        settlementRow.setProcessedAt(LocalDateTime.now());
        settlementRow.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testSalesLedgerCsvContainsBranchAndTerminal() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(reports.salesLedger(null, from, to, null)).thenReturn(List.of(salesRow));

        ExportedReport report = service.salesLedger(null, from, to, "csv", null);
        assertNotNull(report);
        String csv = new String(report.resource().getByteArray(), StandardCharsets.UTF_8);

        assertTrue(csv.contains("Branch Name"));
        assertTrue(csv.contains("Branch Code"));
        assertTrue(csv.contains("Terminal Code"));
        assertTrue(csv.contains("Terminal Type"));
        assertTrue(csv.contains("Ocean Hledan Branch"));
        assertTrue(csv.contains("BR-MC-000001-01"));
        assertTrue(csv.contains("POS-000001"));
    }

    @Test
    void testSettlementsCsvContainsBranchAndTerminal() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(reports.settlements(null, from, to, null)).thenReturn(List.of(settlementRow));

        ExportedReport report = service.settlements(null, from, to, "csv", null);
        assertNotNull(report);
        String csv = new String(report.resource().getByteArray(), StandardCharsets.UTF_8);

        assertTrue(csv.contains("Branch Name"));
        assertTrue(csv.contains("Branch Code"));
        assertTrue(csv.contains("Terminal Code"));
        assertTrue(csv.contains("Terminal Type"));
        assertTrue(csv.contains("Ocean Hledan Branch"));
        assertTrue(csv.contains("BR-MC-000001-01"));
        assertTrue(csv.contains("POS-000001"));
    }

    @Test
    void testSalesLedgerPdfAndXlsxGeneration() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(reports.salesLedger(null, from, to, null)).thenReturn(List.of(salesRow));

        ExportedReport pdfReport = service.salesLedger(null, from, to, "pdf", null);
        assertNotNull(pdfReport);
        assertTrue(pdfReport.resource().getByteArray().length > 0);
        assertEquals("application/pdf", pdfReport.contentType());

        ExportedReport xlsxReport = service.salesLedger(null, from, to, "xlsx", null);
        assertNotNull(xlsxReport);
        assertTrue(xlsxReport.resource().getByteArray().length > 0);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxReport.contentType());
    }

    @Test
    void testSettlementsPdfAndXlsxGeneration() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(reports.settlements(null, from, to, null)).thenReturn(List.of(settlementRow));

        ExportedReport pdfReport = service.settlements(null, from, to, "pdf", null);
        assertNotNull(pdfReport);
        assertTrue(pdfReport.resource().getByteArray().length > 0);
        assertEquals("application/pdf", pdfReport.contentType());

        ExportedReport xlsxReport = service.settlements(null, from, to, "xlsx", null);
        assertNotNull(xlsxReport);
        assertTrue(xlsxReport.resource().getByteArray().length > 0);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxReport.contentType());
    }
}
