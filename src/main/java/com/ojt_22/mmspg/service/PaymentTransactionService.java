package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.PaymentAuthorizeRequestDto;
import com.ojt_22.mmspg.dto.PaymentAuthorizeResponseDto;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.dto.PaymentStatusResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.repository.MerchantFeeRepository; // 👈 MerchantFeeRepository ကို import လုပ်ရန်
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantFeeRepository merchantFeeRepository; // 👈 ဤနေရာတွင် ထည့်သွင်းရန်

    @Transactional
    public PaymentInitiateResponseDto initiateTransaction(PaymentInitiateRequestDto requestDto) {
        
        // 1. Merchant ရှိမရှိ စစ်ဆေးရန်
        Merchant merchant = merchantRepository.findById(requestDto.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        // 2. merchant_fee table ထဲမှ Merchant ID ဖြင့် Fee အချက်အလက်ကို ရှာယူခြင်း
        MerchantFee merchantFee = merchantFeeRepository.findByMerchantId(requestDto.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant fee configuration not found"));

        BigDecimal flatFee = merchantFee.getFlatFee() != null ? merchantFee.getFlatFee() : BigDecimal.ZERO;
        BigDecimal netAmount = requestDto.getAmount().subtract(flatFee);

        String refNo = "TXN-" + System.currentTimeMillis();
        String token = UUID.randomUUID().toString();

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setMerchant(merchant);
        transaction.setOrderId(requestDto.getOrderId());
        transaction.setTransactionReference(refNo);
        transaction.setPaymentToken(token);
        transaction.setAmount(requestDto.getAmount());
        transaction.setCurrency(requestDto.getCurrency() != null ? requestDto.getCurrency() : "MMK");
        
        transaction.setFeeAmount(flatFee);
        transaction.setNetAmount(netAmount);

        transaction.setStatus("INITIATED");
        transaction.setInitiatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        PaymentTransaction savedTxn = transactionRepository.save(transaction);

        return PaymentInitiateResponseDto.builder()
                .transactionReference(savedTxn.getTransactionReference())
                .paymentToken(savedTxn.getPaymentToken())
                .amount(savedTxn.getAmount())
                .currency(savedTxn.getCurrency())
                .status(savedTxn.getStatus())
                .paymentUrl("https://checkout.mmspg.com/pay/" + token)
                .build();
    }

    @Transactional
    public PaymentAuthorizeResponseDto authorizeTransaction(PaymentAuthorizeRequestDto requestDto) {
        PaymentTransaction transaction = transactionRepository.findByPaymentToken(requestDto.getPaymentToken())
                .orElseThrow(() -> new RuntimeException("Transaction not found for token"));

        if (!"INITIATED".equals(transaction.getStatus())) {
            throw new RuntimeException("Transaction is not in INITIATED state");
        }

        transaction.setAuthorizedAt(LocalDateTime.now());
        transaction.setStatus("PENDING_AUTHORIZATION");

        String mockCoreRef = "CORE-" + System.currentTimeMillis();
        transaction.setCoreTransactionReference(mockCoreRef);
        transaction.setStatus("COMPLETED");
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        PaymentTransaction updatedTxn = transactionRepository.save(transaction);

        return PaymentAuthorizeResponseDto.builder()
                .transactionReference(updatedTxn.getTransactionReference())
                .coreTransactionReference(updatedTxn.getCoreTransactionReference())
                .status(updatedTxn.getStatus())
                .completedAt(updatedTxn.getCompletedAt())
                .build();
    }

    public PaymentStatusResponseDto getTransactionStatus(String transactionReference) {
        PaymentTransaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return PaymentStatusResponseDto.builder()
                .transactionReference(transaction.getTransactionReference())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .completedAt(transaction.getCompletedAt())
                .build();
    }

    public List<PaymentTransactionSummaryDto> getMerchantTransactions(UUID merchantId) {
        List<PaymentTransaction> transactions = transactionRepository.findByMerchantId(merchantId);

        return transactions.stream().map(txn -> PaymentTransactionSummaryDto.builder()
                .transactionReference(txn.getTransactionReference())
                .orderId(txn.getOrderId())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .initiatedAt(txn.getInitiatedAt())
                .build())
                .collect(Collectors.toList());
    }
}