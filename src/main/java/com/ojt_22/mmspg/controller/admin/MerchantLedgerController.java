package com.ojt_22.mmspg.controller.admin;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantLedgerResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.service.MerchantLedgerService;

@RestController
@RequestMapping("/api/admin/merchant-ledgers")
public class MerchantLedgerController {

    private final MerchantLedgerService merchantLedgerService;
    
    private final PaymentTransactionRepository paymentTransactionRepository;

    public MerchantLedgerController(
            MerchantLedgerService merchantLedgerService,
            PaymentTransactionRepository paymentTransactionRepository) {

        this.merchantLedgerService = merchantLedgerService;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }
    
    @PostMapping("/{transactionId}")
    public ResponseEntity<MerchantLedgerResponse> createLedger(
            @PathVariable UUID transactionId) {

        PaymentTransaction transaction =
                paymentTransactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException("Transaction not found"));

        Merchant merchant = transaction.getMerchant();

        BigDecimal netAmount = transaction.getNetAmount();
        
        
        MerchantLedgerResponse response =
                merchantLedgerService.createLedgerEntry(
                        merchant,
                        transaction,
                        netAmount);

        return ResponseEntity.ok(response);
    }
}