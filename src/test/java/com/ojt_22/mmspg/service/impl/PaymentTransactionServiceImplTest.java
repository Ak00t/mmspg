package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import com.ojt_22.mmspg.dto.CoreBankingResponseDto;
import com.ojt_22.mmspg.dto.PaymentAuthorizeRequestDto;
import com.ojt_22.mmspg.dto.PaymentAuthorizeResponseDto;
import com.ojt_22.mmspg.dto.PaymentCheckoutInfoDto;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.dto.PaymentStatusResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantBranch;
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.entity.Terminal;
import com.ojt_22.mmspg.enums.PaymentTransactionStatus;
import com.ojt_22.mmspg.repository.MerchantBranchRepository;
import com.ojt_22.mmspg.repository.MerchantFeeRepository;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;
import com.ojt_22.mmspg.service.WebhookDeliveryService;

@ExtendWith(MockitoExtension.class)
public class PaymentTransactionServiceImplTest {

    @Mock private PaymentTransactionRepository transactionRepository;
    @Mock private MerchantRepository merchantRepository;
    @Mock private MerchantFeeRepository merchantFeeRepository;
    @Mock private MerchantBranchRepository branchRepository;
    @Mock private TerminalRepository terminalRepository;
    @Mock private MerchantLedgerRepository ledgerRepository;
    @Mock private WebhookDeliveryService webhookDeliveryService;
    @Mock private CoreBankingClient coreBankingClient;

    @InjectMocks
    private PaymentTransactionServiceImpl transactionService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(transactionService, "paymentRedirectUrl", "http://localhost/pay/");
    }

    @Test
    public void testGetCheckoutInfoByToken_Success() {
        String token = "test-payment-token-123";
        Merchant merchant = new Merchant();
        merchant.setBusinessName("Test Merchant");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentToken(token);
        transaction.setMerchant(merchant);
        transaction.setOrderId("ORD-999");
        transaction.setAmount(BigDecimal.valueOf(15000));
        transaction.setCurrency("MMK");
        transaction.setStatus(PaymentTransactionStatus.INITIATED);

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.of(transaction));

        PaymentCheckoutInfoDto response = transactionService.getCheckoutInfoByToken(token);

        assertNotNull(response);
        assertEquals("Test Merchant", response.getBusinessName());
        assertEquals("ORD-999", response.getOrderId());
        assertEquals(PaymentTransactionStatus.INITIATED.name(), response.getStatus());
        verify(transactionRepository, times(1)).findByPaymentToken(token);
    }
    
    

    @Test
    public void testGetCheckoutInfoByToken_NotFound_ThrowsException() {
        String token = "invalid-token";
        
        // Repository မှ empty Optional ပြန်လာမည်ကို သေချာ Mock လုပ်ခြင်း
        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.empty());

        // Exception အမှန်တကယ် ထွက်လာခြင်း ရှိမရှိ စစ်ဆေးခြင်း
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getCheckoutInfoByToken(token);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Invalid or expired payment token"));
        verify(transactionRepository, times(1)).findByPaymentToken(token);
    }

    @Test
    public void testGetCheckoutInfoByToken_AlreadyProcessed_ThrowsException() {
        String token = "processed-token";
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentToken(token);
        transaction.setStatus(PaymentTransactionStatus.COMPLETED); // 👈 Status ကို INITIATED မဟုတ်ဘဲ အခြားတစ်ခုခု ထားခြင်း

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.of(transaction));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getCheckoutInfoByToken(token);
        });

        assertTrue(exception.getMessage().contains("This transaction has already been processed or is invalid"));
        verify(transactionRepository, times(1)).findByPaymentToken(token);
    }

    @Test
    public void testInitiateTransaction_Success() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-001");
        requestDto.setBranchId(branchId);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        MerchantBranch branch = new MerchantBranch();
        MerchantFee fee = new MerchantFee();
        fee.setFlatFee(BigDecimal.valueOf(100));
        fee.setPercentageRate(BigDecimal.valueOf(1));

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any())).thenReturn(Optional.empty());
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.of(fee));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentInitiateResponseDto response = transactionService.initiateTransaction(requestDto, idempotencyKey);

        assertNotNull(response);
        assertEquals("INITIATED", response.getStatus());
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    public void testInitiateTransaction_IdempotencyExists() {
        String idempotencyKey = "key-123";
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionReference("TXN-123");
        transaction.setPaymentToken("token-123");
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setCurrency("MMK");
        transaction.setStatus(PaymentTransactionStatus.INITIATED);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(transaction));

        PaymentInitiateResponseDto response = transactionService.initiateTransaction(new PaymentInitiateRequestDto(), idempotencyKey);

        assertNotNull(response);
        assertEquals("TXN-123", response.getTransactionReference());
    }

    @Test
    public void testAuthorizeTransaction_Success() {
        String token = "token-abc";
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken(token);
        requestDto.setCustomerId("CUST-001");
        requestDto.setStatus("SUCCESS");

        Merchant merchant = new Merchant();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentToken(token);
        transaction.setStatus(PaymentTransactionStatus.INITIATED);
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setFeeAmount(BigDecimal.valueOf(200));
        transaction.setNetAmount(BigDecimal.valueOf(9800));
        transaction.setMerchant(merchant);
        transaction.setOrderId("ORD-001");
        transaction.setTransactionReference("TXN-123");

        CoreBankingResponseDto coreResponse = new CoreBankingResponseDto();
        coreResponse.setSuccess(true);
        coreResponse.setCoreTransactionRef("CORE-REF-001");

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.of(transaction));
        when(coreBankingClient.executeDebit(any(), any(), any(), any())).thenReturn(coreResponse);
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentAuthorizeResponseDto response = transactionService.authorizeTransaction(requestDto);

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        verify(ledgerRepository, times(1)).save(any());
        verify(webhookDeliveryService, times(1)).sendWebhook(any(), eq("PAYMENT_COMPLETED"));
    }

    @Test
    public void testGetTransactionStatus_Success() {
        String ref = "TXN-123";
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionReference(ref);
        transaction.setOrderId("ORD-001");
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setCurrency("MMK");
        transaction.setStatus(PaymentTransactionStatus.COMPLETED);

        when(transactionRepository.findByTransactionReference(ref)).thenReturn(Optional.of(transaction));

        PaymentStatusResponseDto response = transactionService.getTransactionStatus(ref);

        assertNotNull(response);
        assertEquals(ref, response.getTransactionReference());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    public void testGetMerchantTransactions_Success() {
        UUID merchantId = UUID.randomUUID();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionReference("TXN-123");
        transaction.setOrderId("ORD-001");
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setCurrency("MMK");
        transaction.setStatus(PaymentTransactionStatus.COMPLETED);
        transaction.setInitiatedAt(java.time.LocalDateTime.now());

        when(transactionRepository.findByMerchantId(merchantId)).thenReturn(java.util.List.of(transaction));

        var response = transactionService.getMerchantTransactions(merchantId);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("TXN-123", response.get(0).getTransactionReference());
    }
    
    @Test
    public void testInitiateTransaction_ExistingInitiatedTxn_ReturnsExisting() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        PaymentTransaction existingTxn = new PaymentTransaction();
        existingTxn.setTransactionReference("TXN-OLD");
        existingTxn.setPaymentToken("token-old");
        existingTxn.setAmount(BigDecimal.valueOf(10000));
        existingTxn.setCurrency("MMK");
        existingTxn.setStatus(PaymentTransactionStatus.INITIATED);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.of(existingTxn));

        PaymentInitiateResponseDto response = transactionService.initiateTransaction(requestDto, idempotencyKey);

        assertNotNull(response);
        assertEquals("TXN-OLD", response.getTransactionReference());
    }

    @Test
    public void testInitiateTransaction_BranchAndTerminalNull_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setBranchId(null);
        requestDto.setTerminalId(null);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("At least one of Branch ID or Terminal ID must be provided"));
    }

    @Test
    public void testInitiateTransaction_BranchNotFound_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setBranchId(branchId);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Branch not found"));
    }

    @Test
    public void testInitiateTransaction_TerminalNotFound_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setBranchId(null);
        requestDto.setTerminalId(terminalId);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        MerchantBranch branch = new MerchantBranch();

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(branchRepository.findById(any())).thenReturn(Optional.of(branch));
        when(terminalRepository.findById(terminalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Terminal not found"));
    }

    @Test
    public void testInitiateTransaction_AmountLessThanFee_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setBranchId(branchId);
        requestDto.setAmount(BigDecimal.valueOf(50));

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        MerchantBranch branch = new MerchantBranch();
        MerchantFee fee = new MerchantFee();
        fee.setFlatFee(BigDecimal.valueOf(100));
        fee.setPercentageRate(BigDecimal.ZERO);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.of(fee));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Transaction amount must be greater than or equal to total fee"));
    }

    @Test
    public void testInitiateTransaction_DuplicateCompletedOrder_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(merchantId, "ORD-001", PaymentTransactionStatus.COMPLETED))
                .thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Duplicate transaction"));
    }

    @Test
    public void testAuthorizeTransaction_StatusFailed_Success() {
        String token = "token-abc";
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken(token);
        requestDto.setStatus("FAILED");
        requestDto.setFailureReason("Insufficient balance");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentToken(token);
        transaction.setStatus(PaymentTransactionStatus.INITIATED);

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentAuthorizeResponseDto response = transactionService.authorizeTransaction(requestDto);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        verify(webhookDeliveryService, times(1)).sendWebhook(any(), eq("PAYMENT_FAILED"));
    }

    @Test
    public void testAuthorizeTransaction_CoreBankingFail_Success() {
        String token = "token-abc";
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken(token);
        requestDto.setCustomerId("CUST-001");
        requestDto.setStatus("SUCCESS");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentToken(token);
        transaction.setStatus(PaymentTransactionStatus.INITIATED);
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setFeeAmount(BigDecimal.valueOf(100));

        CoreBankingResponseDto coreResponse = new CoreBankingResponseDto();
        coreResponse.setSuccess(false);
        coreResponse.setFailureReason("Account locked");

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.of(transaction));
        when(coreBankingClient.executeDebit(any(), any(), any(), any())).thenReturn(coreResponse);
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentAuthorizeResponseDto response = transactionService.authorizeTransaction(requestDto);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        verify(webhookDeliveryService, times(1)).sendWebhook(any(), eq("PAYMENT_FAILED"));
    }

    @Test
    public void testInitiateTransaction_MerchantNull_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Merchant not found"));
    }

    @Test
    public void testInitiateTransaction_MerchantInactive_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.SUSPENDED);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Merchant account is not active"));
    }
    
    @Test
    public void testInitiateTransaction_MerchantFeeNotFound_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setBranchId(branchId);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);
        MerchantBranch branch = new MerchantBranch();

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any())).thenReturn(Optional.empty());
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.empty()); // 👈 Fee Configuration မရှိခြင်း

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Merchant fee configuration not found"));
    }

    @Test
    public void testInitiateTransaction_CurrencyNull_DefaultsToMMK() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency(null); // 👈 Currency ကို null ပေးခြင်း (Default MMK သုံးရန်)
        requestDto.setOrderId("ORD-001");
        requestDto.setBranchId(branchId);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        MerchantBranch branch = new MerchantBranch();
        MerchantFee fee = new MerchantFee();
        fee.setFlatFee(BigDecimal.valueOf(100));
        fee.setPercentageRate(BigDecimal.ZERO);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any())).thenReturn(Optional.empty());
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.of(fee));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentInitiateResponseDto response = transactionService.initiateTransaction(requestDto, idempotencyKey);

        assertNotNull(response);
        assertEquals("MMK", response.getCurrency()); // 👈 Default အနေဖြင့် MMK ဖြစ်ရမည်
    }

    @Test
    public void testAuthorizeTransaction_InvalidStatus_ThrowsException() {
        String token = "token-abc";
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken(token);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentToken(token);
        transaction.setStatus(PaymentTransactionStatus.COMPLETED); // 👈 INITIATED မဟုတ်ဘဲ အခြား Status ဖြစ်နေခြင်း

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.of(transaction));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.authorizeTransaction(requestDto);
        });

        assertTrue(exception.getMessage().contains("Transaction has already been processed or is invalid"));
    }
    
    @Test
    public void testInitiateTransaction_FeeValuesNull_HandledSuccessfully() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-001");
        requestDto.setBranchId(branchId);

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        MerchantBranch branch = new MerchantBranch();
        MerchantFee fee = new MerchantFee();
        fee.setFlatFee(null); // 👈 flatFee ကို null ထားခြင်း
        fee.setPercentageRate(null); // 👈 percentageRate ကို null ထားခြင်း

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any())).thenReturn(Optional.empty());
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.of(fee));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentInitiateResponseDto response = transactionService.initiateTransaction(requestDto, idempotencyKey);

        assertNotNull(response);
        assertEquals("INITIATED", response.getStatus());
    }
    
    @Test
    public void testInitiateTransaction_MerchantNullObject_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        // အောက်ပါအတိုင်း merchantRepository.findById က Optional.of(null) ကဲ့သို့ သို့မဟုတ် 
        // Service ထဲက validateMerchant သို့ null ရောက်သွားမည့် အခြေအနေကို ဖန်တီးရန် 
        // (အကယ်၍ findById က null return ပေးပါက Optional.ofNullable(null) သုံးနိုင်သည်)
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Merchant not found"));
    }
    
    @Test
    public void testInitiateTransaction_WithTerminal_Success() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-002");
        requestDto.setTerminalId(terminalId); // 👈 Terminal ID ထည့်ခြင်း

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(com.ojt_22.mmspg.enums.MerchantStatus.ACTIVE);

        Terminal terminal = new Terminal(); // 👈 Terminal entity
        MerchantFee fee = new MerchantFee();
        fee.setFlatFee(BigDecimal.valueOf(100));
        fee.setPercentageRate(BigDecimal.valueOf(1));

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any())).thenReturn(Optional.empty());
        when(terminalRepository.findById(terminalId)).thenReturn(Optional.of(terminal)); // 👈 Terminal ရှာတွေ့ခြင်း
        when(merchantFeeRepository.findByMerchantId(merchantId)).thenReturn(Optional.of(fee));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentInitiateResponseDto response = transactionService.initiateTransaction(requestDto, idempotencyKey);

        assertNotNull(response);
        assertEquals("INITIATED", response.getStatus());
        verify(terminalRepository, times(1)).findById(terminalId);
    }
    
    @Test
    public void testValidateMerchant_NullMerchant_ThrowsException() {
        // Reflection သို့မဟုတ် direct helper/private method test အနေဖြင့် validateMerchant ကို 
        // null merchant ပေး၍ ခေါ်ဆိုခြင်း သို့မဟုတ် initiateTransaction တွင် merchantRepository က null ပြန်ပေးစေခြင်း
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        // findById မှ null တိုက်ရိုက် return ပေးစေရန် (Optional.empty() အစား null)
        when(merchantRepository.findById(merchantId)).thenReturn(null);

        // အကယ်၍ findById က null ဖြစ်နေပါက merchant == null ကို ထိမိစေရန် ဖြစ်သည်
        assertThrows(Exception.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });
    }

    @Test
    public void testGetTransactionStatus_NotFound_ThrowsException() {
        String ref = "INVALID-REF";
        when(transactionRepository.findByTransactionReference(ref)).thenReturn(Optional.empty()); // 👈 Transaction မရှိခြင်း

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactionStatus(ref);
        });

        assertTrue(exception.getMessage().contains("Transaction not found"));
    }
    
    @Test
    public void testAuthorizeTransaction_TokenNotFound_ThrowsException() {
        String token = "invalid-token";
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken(token);

        when(transactionRepository.findByPaymentToken(token)).thenReturn(Optional.empty()); // 👈 Token မရှိခြင်းကို စမ်းသပ်ရန်

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.authorizeTransaction(requestDto);
        });

        assertTrue(exception.getMessage().contains("Invalid or expired payment token"));
    }
    
    @Test
    public void testInitiateTransaction_MerchantStatusNull_ThrowsException() {
        String idempotencyKey = "key-123";
        UUID merchantId = UUID.randomUUID();
        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setOrderId("ORD-001");
        requestDto.setAmount(BigDecimal.valueOf(10000));

        Merchant merchant = new Merchant();
        merchant.setId(merchantId);
        merchant.setStatus(null); // 👈 Status ကို null ထားခြင်းဖြင့် checkStatus branch ကို ဝင်စေရန်

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(transactionRepository.existsByMerchantIdAndOrderIdAndStatus(any(), any(), any())).thenReturn(false);
        when(transactionRepository.findFirstByMerchantIdAndOrderIdAndStatusOrderByCreatedAtDesc(any(), any(), any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.initiateTransaction(requestDto, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Merchant account is not active"));
    }
    
    @Test
    public void testValidateMerchant_Null_ThrowsException() {
        // Reflection ကိုသုံး၍ private validateMerchant(null) မက်သစ်ကို တိုက်ရိုက်ခေါ်ဆိုခြင်း
        Exception exception = assertThrows(Exception.class, () -> {
            ReflectionTestUtils.invokeMethod(transactionService, "validateMerchant", (Merchant) null);
        });

        assertTrue(exception.getMessage().contains("Invalid merchant account"));
    }
   
}