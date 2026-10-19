package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private MerchantLedgerServiceImpl ledgerService;

    @Test
    public void testCreateLedgerEntry_Success() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID());

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(transactionId);
        transaction.setMerchant(merchant);
        transaction.setNetAmount(BigDecimal.valueOf(50000));
        transaction.setOrderId("ORD-001");

        // Optional.of ဖြင့် Transaction ရှိနေကြောင်း Mock ပြုလုပ်ခြင်း
        when(paymentTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        MerchantLedgerEntry savedEntry = new MerchantLedgerEntry();
        savedEntry.setId(UUID.randomUUID());
        savedEntry.setMerchant(merchant);
        savedEntry.setTransaction(transaction);
        savedEntry.setAmount(BigDecimal.valueOf(50000));

        when(merchantLedgerRepository.save(any(MerchantLedgerEntry.class)))
                .thenReturn(savedEntry);

        // Act
        MerchantLedgerResponse response = ledgerService.createLedgerEntry(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(50000), response.getAmount());
        verify(paymentTransactionRepository, times(1)).findById(transactionId);
        verify(merchantLedgerRepository, times(1)).save(any(MerchantLedgerEntry.class));
    }

    @Test
    public void testCreateLedgerEntry_TransactionNotFound_ThrowsException() {
        // Arrange
        UUID transactionId = UUID.randomUUID();

        // Optional.empty() ဖြင့် Transaction မရှိသည့်အခြေအနေကို Mock ပြုလုပ်ခြင်း
        when(paymentTransactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            ledgerService.createLedgerEntry(transactionId);
        });

        assertTrue(exception.getMessage().contains("Transaction not found"));
        verify(paymentTransactionRepository, times(1)).findById(transactionId);
        verify(merchantLedgerRepository, never()).save(any());
    }
}