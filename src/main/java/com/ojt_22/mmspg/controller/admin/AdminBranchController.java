package com.ojt_22.mmspg.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.BranchRequestDto;
import com.ojt_22.mmspg.dto.BranchResponseDto;
import com.ojt_22.mmspg.service.BranchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/branches")
@RequiredArgsConstructor
@Tag(name = "Admin Branch Management", description = "Endpoints for managing merchant branches")
public class AdminBranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Branch", description = "Creates a new branch for a specific merchant.")
    public ResponseEntity<?> createBranch(@Valid @RequestBody BranchRequestDto request) {
        try {
            BranchResponseDto response = branchService.createBranch(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Branches", description = "Retrieves all branches. Optionally filters by merchantId.")
    public ResponseEntity<List<BranchResponseDto>> getAllBranches(
            @RequestParam(required = false) UUID merchantId) {
        List<BranchResponseDto> branches = branchService.getAllBranches(merchantId);
        return ResponseEntity.ok(branches);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Branch", description = "Updates an existing branch.")
    public ResponseEntity<?> updateBranch(
            @PathVariable UUID id, 
            @Valid @RequestBody BranchRequestDto request) {
        try {
            BranchResponseDto response = branchService.updateBranch(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle Branch Status", description = "Toggles branch status between ACTIVE and INACTIVE.")
    public ResponseEntity<?> toggleBranchStatus(@PathVariable UUID id) {
        try {
            branchService.toggleBranchStatus(id);
            return ResponseEntity.ok(Map.of("message", "Branch status toggled successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
