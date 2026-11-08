package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import com.ojt_22.mmspg.dto.DashboardSummaryResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantBranch;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.entity.Terminal;
import com.ojt_22.mmspg.enums.MerchantStatus;
import com.ojt_22.mmspg.enums.PaymentTransactionStatus;
import com.ojt_22.mmspg.enums.TerminalStatus;
import com.ojt_22.mmspg.enums.TerminalType;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private TerminalRepository terminalRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private PaymentTransaction testTransaction;

    @BeforeEach
    void setUp() {
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID());
        merchant.setMerchantCode("MC-000001");
        merchant.setBusinessName("Ocean Supercenter");

        MerchantBranch branch = new MerchantBranch();
        branch.setId(UUID.randomUUID());
        branch.setBranchName("Ocean Hledan Branch");
        branch.setBranchCode("BR-MC-000001-01");

        Terminal terminal = new Terminal();
        terminal.setId(UUID.randomUUID());
        terminal.setTerminalCode("POS-000001");
        terminal.setTerminalType(TerminalType.PHYSICAL_POS);

        testTransaction = new PaymentTransaction();
        testTransaction.setId(UUID.randomUUID());
        testTransaction.setTransactionReference("TXN-12345");
        testTransaction.setOrderId("ORD-001");
        testTransaction.setMerchant(merchant);
        testTransaction.setBranch(branch);
        testTransaction.setTerminal(terminal);
        testTransaction.setAmount(new BigDecimal("50000.00"));
        testTransaction.setCurrency("MMK");
        testTransaction.setStatus(PaymentTransactionStatus.COMPLETED);
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetDashboardSummary_IncludesLiveTransactionsWithBranchAndTerminal() {
        when(merchantRepository.countByStatus(MerchantStatus.PENDING)).thenReturn(2L);
        when(paymentTransactionRepository.sumAmountByCreatedAtBetween(any(), any()))
                .thenReturn(Optional.of(new BigDecimal("150000.00")));
        when(terminalRepository.countByStatus(TerminalStatus.ONLINE)).thenReturn(5L);
        when(staffUserRepository.count()).thenReturn(3L);
        when(merchantRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(paymentTransactionRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(testTransaction));

        DashboardSummaryResponse response = adminDashboardService.getDashboardSummary();

        assertNotNull(response);
        assertEquals(2L, response.getTotalPendingApprovals());
        assertEquals(new BigDecimal("150000.00"), response.getTotalVolumeToday());
        assertEquals(5L, response.getActiveGateways());
        assertEquals(3L, response.getInternalStaff());
        assertNotNull(response.getRecentTransactions());
        assertEquals(1, response.getRecentTransactions().size());

        var liveTx = response.getRecentTransactions().get(0);
        assertEquals("TXN-12345", liveTx.getTransactionReference());
        assertEquals("ORD-001", liveTx.getOrderId());
        assertEquals("Ocean Supercenter", liveTx.getMerchantName());
        assertEquals("MC-000001", liveTx.getMerchantCode());
        assertEquals("Ocean Hledan Branch", liveTx.getBranchName());
        assertEquals("BR-MC-000001-01", liveTx.getBranchCode());
        assertEquals("POS-000001", liveTx.getTerminalCode());
        assertEquals("PHYSICAL_POS", liveTx.getTerminalType());
        assertEquals(new BigDecimal("50000.00"), liveTx.getAmount());
        assertEquals("COMPLETED", liveTx.getStatus());
    }
}
