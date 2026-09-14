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
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.StaffRequestDto;
import com.ojt_22.mmspg.dto.StaffResponseDto;
import com.ojt_22.mmspg.service.StaffUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/staff-users")
@RequiredArgsConstructor
@Tag(name = "Admin Staff Management", description = "Endpoints for managing Bank Admins and Staff")
public class AdminStaffController {

    private final StaffUserService staffUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Staff", description = "Retrieves a list of all staff members.")
    public ResponseEntity<List<StaffResponseDto>> getAllStaff() {
        return ResponseEntity.ok(staffUserService.getAllStaffUsersDto());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Staff User", description = "Creates a new staff user with encrypted password.")
    public ResponseEntity<?> createStaffUser(@Valid @RequestBody StaffRequestDto request) {
        try {
            StaffResponseDto response = staffUserService.createStaffUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle Staff Status", description = "Toggles a staff member's status between ACTIVE and DISABLED.")
    public ResponseEntity<?> toggleStaffStatus(@PathVariable UUID id) {
        try {
            staffUserService.toggleStaffStatus(id);
            return ResponseEntity.ok(Map.of("message", "Staff status toggled successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
