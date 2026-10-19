package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ojt_22.mmspg.dto.PaymentCheckoutInfoDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.enums.PaymentTransactionStatus;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentTransactionServiceImplTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @InjectMocks
    private PaymentTransactionServiceImpl transactionService;

    @Test
    public void testGetCheckoutInfoByToken_Success() {
        // Arrange
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

        // Optional.of ဖြင့် Token တွေ့ရှိကြောင်း Mock ပြုလုပ်ခြင်း  
        when(transactionRepository.findByPaymentToken(token))
                .thenReturn(Optional.of(transaction));

        // Act
        PaymentCheckoutInfoDto response = transactionService.getCheckoutInfoByToken(token);

        // Assert
        assertNotNull(response);
        assertEquals("Test Merchant", response.getBusinessName());
        assertEquals("ORD-999", response.getOrderId());
        assertEquals(PaymentTransactionStatus.INITIATED.name(), response.getStatus());
        verify(transactionRepository, times(1)).findByPaymentToken(token);
    }

    @Test
    public void testGetCheckoutInfoByToken_NotFound_ThrowsException() {
        // Arrange
        String token = "invalid-token";

        // Optional.empty() ဖြင့် Token မရှိကြောင်း Mock ပြုလုပ်ခြင်း
        when(transactionRepository.findByPaymentToken(token))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getCheckoutInfoByToken(token);
        });

        assertTrue(exception.getMessage().contains("Invalid or expired payment token"));
        verify(transactionRepository, times(1)).findByPaymentToken(token);
    }
}