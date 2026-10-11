package com.ojt_22.mmspg.controller.admin;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.DailySettlementResponse;
import com.ojt_22.mmspg.dto.SettlementResponse;
import com.ojt_22.mmspg.service.SettlementService;
import java.util.List;

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
    
    @PostMapping("/calculate-gross")
    public ResponseEntity<BigDecimal> calculateGrossAmount(
            @RequestBody List<UUID> transactionIds) {

        BigDecimal grossAmount =
                settlementService.calculateGrossAmountByTransactionIds(transactionIds);

        return ResponseEntity.ok(grossAmount);
    }
    
    @PostMapping("/calculate-fees")
    public ResponseEntity<BigDecimal> calculateTotalFees(
            @RequestBody List<UUID> transactionIds) {

        BigDecimal totalFees =
                settlementService.calculateTotalFeesByTransactionIds(transactionIds);

        return ResponseEntity.ok(totalFees);
    }
    
    @PostMapping("/calculate-net")
    public ResponseEntity<BigDecimal> calculateNetSettlement(
            @RequestBody List<UUID> transactionIds) {

        BigDecimal netSettlement =
                settlementService.calculateNetSettlementByTransactionIds(
                        transactionIds);

        return ResponseEntity.ok(netSettlement);
    }
    
    @PostMapping("/run-daily")
    public ResponseEntity<DailySettlementResponse> runDailySettlement() {

        DailySettlementResponse response =
                settlementService.runDailySettlement();

        return ResponseEntity.ok(response);
    }
}