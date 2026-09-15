package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.ojt_22.mmspg.dto.MerchantLedgerResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantLedgerEntry;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;

@Service
@RequiredArgsConstructor
public class MerchantLedgerService {

    private final MerchantLedgerRepository merchantLedgerRepository;

    public MerchantLedgerService(
            MerchantLedgerRepository merchantLedgerRepository) {

        this.merchantLedgerRepository = merchantLedgerRepository;
    }
    
    public MerchantLedgerResponse createLedgerEntry(
            Merchant merchant,
            PaymentTransaction transaction,
            BigDecimal netAmount) {

        MerchantLedgerEntry ledger = new MerchantLedgerEntry();

        ledger.setMerchant(merchant);
        ledger.setTransaction(transaction);
        ledger.setAmount(netAmount);

        ledger.setEntryType("CREDIT");
        ledger.setBalanceType("PENDING");
        ledger.setDescription("Payment completed");

        MerchantLedgerEntry savedLedger =
                merchantLedgerRepository.save(ledger);

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
    /**
     * Ledger ID ဖြင့် balance_type ကို SETTLED သို့ ပြောင်းလဲခြင်း
     */
    @Transactional
    public void markLedgerAsSettled(UUID ledgerId) {
        int updatedRows = ledgerRepository.updateBalanceType(ledgerId, "SETTLED");
        
        if (updatedRows == 0) {
            throw new RuntimeException("Ledger entry not found with ID: " + ledgerId);
        }
    }

}