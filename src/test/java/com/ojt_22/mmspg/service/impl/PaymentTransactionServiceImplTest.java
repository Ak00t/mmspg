package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ojt_22.mmspg.client.CoreBankingClient;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.enums.MerchantStatus;
import com.ojt_22.mmspg.repository.MerchantBranchRepository;
import com.ojt_22.mmspg.repository.MerchantFeeRepository;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;
import com.ojt_22.mmspg.service.WebhookDeliveryService;

@ExtendWith(MockitoExtension.class)
public class PaymentTransactionServiceImplTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private MerchantFeeRepository merchantFeeRepository;
    @Mock
    private MerchantBranchRepository branchRepository;
    @Mock
    private TerminalRepository terminalRepository;
    @Mock
    private MerchantLedgerRepository ledgerRepository;
    @Mock
    private WebhookDeliveryService webhookDeliveryService;
    @Mock
    private CoreBankingClient coreBankingClient;

    @InjectMocks
    private PaymentTransactionServiceImpl paymentTransactionService;

    @BeforeEach
    public void setUp() {
        // @Value("${payment.gateway.redirect-url}") အတွက် Value ထည့်ပေးခြင်း
        ReflectionTestUtils.setField(paymentTransactionService, "paymentRedirectUrl", "http://localhost:8080/checkout?token=");
    }

    @Test
    public void testInitiateTransaction_Success() {
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-123");
        requestDto.setAmount(new BigDecimal("10000.00"));
        requestDto.setCurrency("MMK");
        // Branch ID သို့မဟုတ် Terminal ID တစ်ခုခု ပေးရမည်
        requestDto.setBranchId(UUID.randomUUID());

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(MerchantStatus.ACTIVE);

        MerchantFee fee = new MerchantFee();
        fee.setFlatFee(new BigDecimal("100.00"));
        fee.setPercentageRate(new BigDecimal("1.00"));

        when(transactionRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderId(merchantId, "ORD-123")).thenReturn(false);
        when(branchRepository.findById(any(UUID.randomUUID().getClass()))).thenReturn(Optional.of(new com.ojt_22.mmspg.entity.MerchantBranch()));
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.of(fee));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentInitiateResponseDto response = paymentTransactionService.initiateTransaction(requestDto, "idem-key-1");

        assertNotNull(response);
        assertNotNull(response.getPaymentToken());
        assertNotNull(response.getPaymentUrl());
    }
}