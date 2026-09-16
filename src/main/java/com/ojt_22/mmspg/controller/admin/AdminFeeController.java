package com.ojt_22.mmspg.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantFeeRequestDto;
import com.ojt_22.mmspg.dto.MerchantFeeResponseDto;
import com.ojt_22.mmspg.service.MerchantFeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/fees")
@RequiredArgsConstructor
@Tag(name = "Admin Fee Configuration", description = "Endpoints for managing Merchant Fee Rules")
public class AdminFeeController {

    private final MerchantFeeService merchantFeeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all Merchant Fee rules", description = "Retrieves all fee configurations joined with merchant names.")
    public ResponseEntity<List<MerchantFeeResponseDto>> getAllFees() {
        return ResponseEntity.ok(merchantFeeService.getAllFeesWithMerchantNames());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Merchant Fee", description = "Creates a new fee rule (PERCENTAGE, FLAT, or MIXED) for a merchant.")
    public ResponseEntity<?> createMerchantFee(@Valid @RequestBody MerchantFeeRequestDto request) {
        try {
            MerchantFeeResponseDto response = merchantFeeService.createMerchantFee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
