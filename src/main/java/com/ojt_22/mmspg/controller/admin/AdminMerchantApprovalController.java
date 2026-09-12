package com.ojt_22.mmspg.controller.admin;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantPendingDto;
import com.ojt_22.mmspg.dto.MerchantRejectRequest;
import com.ojt_22.mmspg.service.MerchantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "Admin Merchant Approvals", description = "Endpoints for approving or rejecting merchant registrations")
public class AdminMerchantApprovalController {

    private final MerchantService merchantService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Pending Merchants", description = "Retrieves a paginated list of merchants with PENDING status.")
    public ResponseEntity<Page<MerchantPendingDto>> getPendingMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<MerchantPendingDto> pendingMerchants = merchantService.getPendingMerchants(page, size);
        return ResponseEntity.ok(pendingMerchants);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve Merchant", description = "Approves a pending merchant and updates status to ACTIVE.")
    public ResponseEntity<?> approveMerchant(@PathVariable UUID id) {
        try {
            merchantService.approveMerchant(id);
            return ResponseEntity.ok(Map.of("message", "Merchant approved successfully."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject Merchant", description = "Rejects a pending merchant and updates status to REJECTED.")
    public ResponseEntity<?> rejectMerchant(
            @PathVariable UUID id, 
            @RequestBody(required = false) MerchantRejectRequest rejectRequest) {
        try {
            String reason = rejectRequest != null ? rejectRequest.getReason() : null;
            merchantService.rejectMerchant(id, reason);
            return ResponseEntity.ok(Map.of("message", "Merchant rejected successfully."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
