package com.ojt_22.mmspg.controller.admin;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantLedgerResponse;
import com.ojt_22.mmspg.service.MerchantLedgerService;

@RestController
@RequestMapping("/api/admin/merchant-ledgers")
public class MerchantLedgerController {

    private final MerchantLedgerService merchantLedgerService;

    public MerchantLedgerController(
            MerchantLedgerService merchantLedgerService) {

        this.merchantLedgerService = merchantLedgerService;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{transactionId}")
    public ResponseEntity<MerchantLedgerResponse> createLedger(
            @PathVariable UUID transactionId) {

        MerchantLedgerResponse response =
                merchantLedgerService.createLedgerEntry(transactionId);

        return ResponseEntity.ok(response);
    }
}