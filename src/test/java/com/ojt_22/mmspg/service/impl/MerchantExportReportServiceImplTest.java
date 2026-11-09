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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ojt_22.mmspg.dto.SalesLedgerReportRow;
import com.ojt_22.mmspg.dto.SettlementReportRow;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.MerchantExportReportRepository;
import com.ojt_22.mmspg.service.ExportedReport;

@ExtendWith(MockitoExtension.class)
class MerchantExportReportServiceImplTest {

    @Mock
    private MerchantExportReportRepository reports;

    @Mock
    private MerchantRepository merchants;

    @InjectMocks
    private MerchantExportReportServiceImpl service;

    private UUID merchantId;
    private Merchant merchant;
    private SalesLedgerReportRow salesRow;
    private SettlementReportRow settlementRow;

    @BeforeEach
    void setUp() {
        merchantId = UUID.randomUUID();
        merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setMerchantCode("MC-000001");
        merchant.setBusinessName("Ocean Supercenter");
        merchant.setEmail("ocean@gmail.com");
        merchant.setPhone("09987654321");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ocean@gmail.com", "credentials",
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))));

        salesRow = new SalesLedgerReportRow();
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
    void testMerchantSalesLedgerCsvContainsBranchAndTerminal() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(merchants.findByEmail("ocean@gmail.com")).thenReturn(Optional.of(merchant));
        when(reports.salesLedger(merchantId, from, to, null)).thenReturn(List.of(salesRow));

        ExportedReport report = service.salesLedger(from, to, "csv", null);
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
    void testMerchantSettlementsCsvContainsBranchAndTerminal() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(merchants.findByEmail("ocean@gmail.com")).thenReturn(Optional.of(merchant));
        when(reports.settlements(merchantId, from, to, null)).thenReturn(List.of(settlementRow));

        ExportedReport report = service.settlements(from, to, "csv", null);
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
    void testMerchantSalesLedgerPdfAndXlsxGeneration() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(merchants.findByEmail("ocean@gmail.com")).thenReturn(Optional.of(merchant));
        when(reports.salesLedger(merchantId, from, to, null)).thenReturn(List.of(salesRow));

        ExportedReport pdfReport = service.salesLedger(from, to, "pdf", null);
        assertNotNull(pdfReport);
        assertTrue(pdfReport.resource().getByteArray().length > 0);
        assertEquals("application/pdf", pdfReport.contentType());

        ExportedReport xlsxReport = service.salesLedger(from, to, "xlsx", null);
        assertNotNull(xlsxReport);
        assertTrue(xlsxReport.resource().getByteArray().length > 0);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxReport.contentType());
    }

    @Test
    void testMerchantSettlementsPdfAndXlsxGeneration() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(merchants.findByEmail("ocean@gmail.com")).thenReturn(Optional.of(merchant));
        when(reports.settlements(merchantId, from, to, null)).thenReturn(List.of(settlementRow));

        ExportedReport pdfReport = service.settlements(from, to, "pdf", null);
        assertNotNull(pdfReport);
        assertTrue(pdfReport.resource().getByteArray().length > 0);
        assertEquals("application/pdf", pdfReport.contentType());

        ExportedReport xlsxReport = service.settlements(from, to, "xlsx", null);
        assertNotNull(xlsxReport);
        assertTrue(xlsxReport.resource().getByteArray().length > 0);
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxReport.contentType());
    }
}
