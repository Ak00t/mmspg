package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ojt_22.mmspg.dto.MerchantLedgerResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantLedgerEntry;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.exception.ResourceNotFoundException;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;

@ExtendWith(MockitoExtension.class)
public class MerchantLedgerServiceImplTest {

    @Mock
    private MerchantLedgerRepository merchantLedgerRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks
    private MerchantLedgerServiceImpl merchantLedgerService;

    @Test
    public void testMarkLedgerAsSettled_Success() {
        UUID ledgerId = UUID.randomUUID();
        when(merchantLedgerRepository.updateBalanceType(ledgerId, "SETTLED")).thenReturn(1);

        // Test method execution
        merchantLedgerService.markLedgerAsSettled(ledgerId);

        verify(merchantLedgerRepository, times(1)).updateBalanceType(ledgerId, "SETTLED");
    }

    @Test
    public void testMarkLedgerAsSettled_NotFound() {
        UUID ledgerId = UUID.randomUUID();
        when(merchantLedgerRepository.updateBalanceType(ledgerId, "SETTLED")).thenReturn(0);

        assertThrows(RuntimeException.class, () -> {
            merchantLedgerService.markLedgerAsSettled(ledgerId);
        });
    }

    @Test
    public void testCreateLedgerEntry_Success() {
        UUID transactionId = UUID.randomUUID();
        
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID());

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(transactionId);
        transaction.setMerchant(merchant);
        transaction.setNetAmount(new BigDecimal("5000.00"));
        transaction.setOrderId("ORD-001");

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(merchantLedgerRepository.save(any(MerchantLedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MerchantLedgerResponse response = merchantLedgerService.createLedgerEntry(transactionId);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals(transactionId, response.getTransactionId());
    }

    @Test
    public void testCreateLedgerEntry_TransactionNotFound() {
        UUID transactionId = UUID.randomUUID();
        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            merchantLedgerService.createLedgerEntry(transactionId);
        });
    }
}