package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ojt_22.mmspg.dto.MerchantLedgerResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantLedgerEntry;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.exception.ResourceNotFoundException;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;

@Service
@RequiredArgsConstructor
public class MerchantLedgerService {

    private final MerchantLedgerRepository merchantLedgerRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public MerchantLedgerResponse createLedgerEntry(UUID transactionId) {

        // 1. Transaction ရှာမယ်
        PaymentTransaction transaction =
                paymentTransactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found: " + transactionId));

        // 2. Transaction ကနေ Merchant ယူမယ်
        Merchant merchant = transaction.getMerchant();

        // 3. Transaction ကနေ Net Amount ယူမယ်
        BigDecimal netAmount = transaction.getNetAmount();

        // 4. Ledger Entity ဆောက်မယ်
        MerchantLedgerEntry ledger = new MerchantLedgerEntry();

        ledger.setMerchant(merchant);
        ledger.setTransaction(transaction);
        ledger.setAmount(netAmount);
        ledger.setEntryType("CREDIT");
        ledger.setBalanceType("PENDING");
        ledger.setDescription("Payment completed");

        // 5. Database ထဲ save မယ်
        MerchantLedgerEntry savedLedger =
                merchantLedgerRepository.save(ledger);

        // 6. Response DTO ပြောင်းမယ်
        MerchantLedgerResponse response =
                new MerchantLedgerResponse();

        response.setLedgerId(savedLedger.getId());
        response.setMerchantId(savedLedger.getMerchant().getId());
        response.setTransactionId(savedLedger.getTransaction().getId());
        response.setAmount(savedLedger.getAmount());
        response.setEntryType(savedLedger.getEntryType());
        response.setBalanceType(savedLedger.getBalanceType());
        response.setDescription(savedLedger.getDescription());
        response.setCreatedAt(savedLedger.getCreatedAt());

        return response;
    }

    @Transactional
    public void markLedgerAsSettled(UUID ledgerId) {

        int updatedRows =
        		merchantLedgerRepository.updateBalanceType(
        		        ledgerId,
        		        "CLEARED");

        if (updatedRows == 0) {
            throw new ResourceNotFoundException(
                    "Ledger entry not found with ID: " + ledgerId);
        }
    }
}