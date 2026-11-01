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
import com.ojt_22.mmspg.annotation.Auditable;
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
    @Auditable(menuName = "Merchant Approval", action = "APPROVE", targetType = "MERCHANT", targetId = "#id")
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
    @Auditable(menuName = "Merchant Approval", action = "REJECT", targetType = "MERCHANT", targetId = "#id")
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
    
    @PutMapping("/{id}")
    @Auditable(menuName = "Merchant Management", action = "UPDATE", targetType = "MERCHANT", targetId = "#id")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Update Merchant", description = "Updates merchant business name and registration number.")
    public ResponseEntity<?> updateMerchant(
            @PathVariable UUID id, 
            @RequestBody Map<String, String> requestBody) {
        try {
            String businessName = requestBody.get("businessName");
            String merchantCode = requestBody.get("merchantCode");
            
            // Service ကို လှမ်းခေါ်မည် (ဤနေရာတွင် Notification ပါ အလုပ်လုပ်သွားပါမည်)
            merchantService.updateMerchant(id, businessName, merchantCode);
            
            return ResponseEntity.ok(Map.of("message", "Merchant updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Auditable(menuName = "Merchant Management", action = "UPDATE_STATUS", targetType = "MERCHANT", targetId = "#id")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Update Merchant Status", description = "Updates merchant status (e.g., SUSPENDED, ACTIVE) with a reason.")
    public ResponseEntity<?> updateMerchantStatus(
            @PathVariable UUID id, 
            @RequestParam String status, 
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String reason = requestBody != null ? requestBody.get("reason") : "No reason provided";
            
            // Service ကို လှမ်းခေါ်မည် (ဤနေရာတွင် Notification ပါ အလုပ်လုပ်သွားပါမည်)
            merchantService.updateMerchantStatus(id, status, reason);
            
            return ResponseEntity.ok(Map.of("message", "Merchant status updated to " + status + " successfully."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

