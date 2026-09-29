package com.ojt_22.mmspg.controller.admin;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.SettlementResponse;
import com.ojt_22.mmspg.service.SettlementService;

@RestController
@RequestMapping("/api/admin/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/{transactionId}")
    public ResponseEntity<SettlementResponse> processSettlement(
            @PathVariable UUID transactionId) {

        SettlementResponse response =
                settlementService.processSettlement(transactionId);

        return ResponseEntity.ok(response);
    }
}