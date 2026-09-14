package com.ojt_22.mmspg.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantLedgerService {

    private final MerchantLedgerRepository ledgerRepository;

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