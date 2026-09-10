package com.ojt_22.mmspg.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.entity.MerchantLedgerEntry;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/merchant-ledgers")
@RequiredArgsConstructor
public class MerchantLedgerApiController {

    private final MerchantLedgerRepository ledgerRepository;

    /**
     * PENDING သို့မဟုတ် SETTLED balance_type အလိုက် Ledger စာရင်းများ ရှာယူရန်
     * GET /api/v1/merchant-ledgers/by-balance-type?balanceType=PENDING
     */
    @GetMapping("/by-balance-type")
    public ResponseEntity<List<MerchantLedgerEntry>> getByBalanceType(@RequestParam String balanceType) {
        return ResponseEntity.ok(ledgerRepository.findByBalanceType(balanceType));
    }

    /**
     * Specific Ledger ID ၏ balance_type (PENDING) ကို SETTLED သို့ Update ပြုလုပ်ရန်
     * PUT /api/v1/merchant-ledgers/{ledgerId}/balance-type?status=SETTLED
     */
    @PutMapping("/{ledgerId}/balance-type")
    @Transactional
    public ResponseEntity<String> updateBalanceType(
            @PathVariable UUID ledgerId,
            @RequestParam(defaultValue = "SETTLED") String status) {

        int updatedRows = ledgerRepository.updateBalanceType(ledgerId, status);

        if (updatedRows > 0) {
            return ResponseEntity.ok("Ledger balance type updated successfully to " + status);
        } else {
            return ResponseEntity.badRequest().body("Ledger entry not found with ID: " + ledgerId);
        }
    }
}