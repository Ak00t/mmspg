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

import com.ojt_22.mmspg.dto.TerminalRequestDto;
import com.ojt_22.mmspg.dto.TerminalResponseDto;
import com.ojt_22.mmspg.service.TerminalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/terminals")
@RequiredArgsConstructor
@Tag(name = "Admin Terminal Management", description = "Endpoints for provisioning and managing Terminals")
public class AdminTerminalController {

    private final TerminalService terminalService;

    @PostMapping("/provision")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provision Terminal", description = "Provisions a new physical POS or virtual API terminal for a specific branch.")
    public ResponseEntity<?> provisionTerminal(@Valid @RequestBody TerminalRequestDto request) {
        try {
            TerminalResponseDto response = terminalService.provisionTerminal(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Terminals", description = "Retrieves a list of all terminals with merchant and branch names.")
    public ResponseEntity<List<TerminalResponseDto>> getAllTerminals() {
        return ResponseEntity.ok(terminalService.getAllTerminals());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Terminal Status", description = "Updates a terminal's status to ONLINE, OFFLINE, or SUSPENDED.")
    public ResponseEntity<?> updateTerminalStatus(
            @PathVariable UUID id, 
            @RequestParam String status) {
        try {
            terminalService.updateTerminalStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Terminal status updated successfully to " + status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
