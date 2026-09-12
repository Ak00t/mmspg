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

import com.ojt_22.mmspg.dto.CoreBankingResponseDto;
import com.ojt_22.mmspg.client.CoreBankingClient;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantBranch; 
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.entity.MerchantLedgerEntry;
import com.ojt_22.mmspg.entity.Terminal; 
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.repository.MerchantBranchRepository; 
import com.ojt_22.mmspg.repository.MerchantFeeRepository;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantFeeRepository merchantFeeRepository;
    
    private final MerchantBranchRepository branchRepository; 
    private final TerminalRepository terminalRepository; 
    private final MerchantLedgerRepository ledgerRepository;

    // Group 2 (Core Banking System) သို့ API လှမ်းခေါ်မည့် Client Class
    private final CoreBankingClient coreBankingClient;

    @Transactional
    public PaymentInitiateResponseDto initiateTransaction(PaymentInitiateRequestDto requestDto) {
        
        // 1. Merchant ရှိမရှိ စစ်ဆေးရန်
        Merchant merchant = merchantRepository.findById(requestDto.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        // 2. Branch ရှိမရှိ စစ်ဆေးရန်
        MerchantBranch branch = branchRepository.findById(requestDto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // 3. Terminal ရှိမရှိ စစ်ဆေးရန်
        Terminal terminal = terminalRepository.findById(requestDto.getTerminalId())
                .orElseThrow(() -> new RuntimeException("Terminal not found"));

        // 4. Fee အချက်အလက်ကို ရှာယူခြင်း
        MerchantFee merchantFee = merchantFeeRepository.findByMerchantId(requestDto.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant fee configuration not found"));

        BigDecimal flatFee = merchantFee.getFlatFee() != null ? merchantFee.getFlatFee() : BigDecimal.ZERO;
        BigDecimal netAmount = requestDto.getAmount().subtract(flatFee);

        String refNo = "TXN-" + System.currentTimeMillis();
        String token = UUID.randomUUID().toString();

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setMerchant(merchant);
        transaction.setBranch(branch); 
        transaction.setTerminal(terminal); 
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

        // Group 1 (Banking Customer Portal) ၏ Redirect URL သို့ Token ဖြင့် လမ်းကြောင်းပေးခြင်း
        return PaymentInitiateResponseDto.builder()
                .transactionReference(savedTxn.getTransactionReference())
                .paymentToken(savedTxn.getPaymentToken())
                .amount(savedTxn.getAmount())
                .currency(savedTxn.getCurrency())
                .status(savedTxn.getStatus())
                .paymentUrl("https://customer-portal.group1bank.com/checkout?token=" + token)
                .build();
    }

    @Transactional
    public PaymentAuthorizeResponseDto authorizeTransaction(PaymentAuthorizeRequestDto requestDto) {
        // 1. Payment Token ဖြင့် Transaction ရှာဖွေခြင်း
        PaymentTransaction transaction = transactionRepository.findByPaymentToken(requestDto.getPaymentToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired payment token"));

        // 2. Transaction Status စစ်ဆေးခြင်း (INITIATED ဖြစ်မှသာ ဆက်လုပ်မည်)
        if (!"INITIATED".equals(transaction.getStatus())) {
            throw new RuntimeException("Transaction has already been processed or is invalid");
        }
        
        BigDecimal feeAmount = transaction.getFeeAmount(); // Fee Amount ရယူခြင်း

        CoreBankingResponseDto coreBankingResponse = coreBankingClient.executeDebit(
                requestDto.getCustomerId(),
                transaction.getAmount(),
                feeAmount, // <-- Fee Amount ပါ ထည့်သွင်းပေးလိုက်ပါသည်
                transaction.getTransactionReference()
        );

        // 4. Core Banking မှ Fail ဖြစ်လာပါက Transaction ကို FAILED ပြောင်း၍ သိမ်းခြင်း
        if (!coreBankingResponse.isSuccess()) {
            transaction.setStatus("FAILED");
            transaction.setFailureReason(coreBankingResponse.getFailureReason());
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            return PaymentAuthorizeResponseDto.builder()
                    .transactionReference(transaction.getTransactionReference())
                    .status("FAILED")
                    .failureReason(coreBankingResponse.getFailureReason())
                    .build();
        }

        // 5. Core Banking မှ Success ဖြစ်ပါက Transaction ကို COMPLETED ပြောင်းခြင်း
        transaction.setStatus("COMPLETED");
        transaction.setCoreTransactionReference(coreBankingResponse.getCoreTransactionRef());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        PaymentTransaction completedTxn = transactionRepository.save(transaction);

        // 6. Merchant ၏ Ledger ထဲသို့ PENDING Balance အဖြစ် CREDIT မှတ်တမ်းထည့်ခြင်း
        MerchantLedgerEntry ledgerEntry = new MerchantLedgerEntry();
        ledgerEntry.setMerchant(completedTxn.getMerchant());
        ledgerEntry.setTransaction(completedTxn);
        ledgerEntry.setEntryType("CREDIT");
        ledgerEntry.setBalanceType("PENDING");
        ledgerEntry.setAmount(completedTxn.getNetAmount());
        ledgerEntry.setDescription("Payment settlement for Order ID: " + completedTxn.getOrderId());
        
        ledgerRepository.save(ledgerEntry);

        // 7. Response ပြန်လည်ထုတ်ပေးခြင်း
        return PaymentAuthorizeResponseDto.builder()
                .transactionReference(completedTxn.getTransactionReference())
                .orderId(completedTxn.getOrderId())
                .amount(completedTxn.getAmount())
                .currency(completedTxn.getCurrency())
                .status(completedTxn.getStatus())
                .completedAt(completedTxn.getCompletedAt())
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