package com.ojt_22.mmspg.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.service.TerminalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/terminals")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin Terminal Management", description = "Endpoints for provisioning and managing Terminals")
public class AdminTerminalController {

    private final TerminalService terminalService;

    // Terminal အသစ်ဖန်တီးခြင်း (ADMIN သာ ပြုလုပ်ခွင့်ရှိသည် - SUPPORT မပါပါ)
    @PostMapping("/provision")
    @Auditable(menuName = "Terminal Management", action = "PROVISION", targetType = "TERMINAL", targetId = "#result?.body?.terminalId")
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

    // Terminal စာရင်းကြည့်ခြင်း (ADMIN, SUPPORT, AUDITOR အားလုံး ကြည့်ရှုခွင့်ရှိသည်)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Get All Terminals", description = "Retrieves a list of all terminals with merchant and branch names.")
    public ResponseEntity<List<TerminalResponseDto>> getAllTerminals() {
        return ResponseEntity.ok(terminalService.getAllTerminals());
    }

    // Terminal Status ပြောင်းလဲခြင်း (ADMIN နှင့် SUPPORT ပြင်ဆင်ခွင့်ရှိသည်)
    @PutMapping("/{id}/status")
    @Auditable(menuName = "Terminal Management", action = "UPDATE_STATUS", targetType = "TERMINAL", targetId = "#id")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Update Terminal Status", description = "Updates a terminal's status to ONLINE, OFFLINE, SUSPENDED, MAINTENANCE, or INACTIVE.")
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

    // Terminal အချက်အလက်များ ပြင်ဆင်ခြင်း (ADMIN သာ ပြင်ဆင်ခွင့်ရှိသည် - SUPPORT မပါပါ)
    @PutMapping("/{id}")
    @Auditable(menuName = "Terminal Management", action = "UPDATE", targetType = "TERMINAL", targetId = "#id")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Edit Terminal", description = "Updates terminal name and type.")
    public ResponseEntity<?> editTerminal(
            @PathVariable UUID id, 
            @RequestBody TerminalRequestDto request) {
        try {
            TerminalResponseDto response = terminalService.updateTerminal(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Terminal ပြန်လည်အသုံးပြုခြင်း / Cancel Ban (ADMIN နှင့် SUPPORT ခွင့်ပြုပါသည်)
    @PutMapping(value = {"/{id}/reactivate", "/{id}/cancel-ban"})
    @Auditable(menuName = "Terminal Management", action = "CANCEL_BAN", targetType = "TERMINAL", targetId = "#id")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Cancel Ban / Reactivate Terminal", description = "Cancels ban for a suspended terminal and sets its status back to ONLINE.")
    public ResponseEntity<?> reactivateTerminal(@PathVariable UUID id) {
        try {
            terminalService.updateTerminalStatus(id, "ONLINE");
            return ResponseEntity.ok(Map.of("message", "Terminal ban cancelled successfully, status set to ONLINE"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Terminal ဆိုင်းငံ့ခြင်း / Ban (ADMIN နှင့် SUPPORT ခွင့်ပြုပါသည်)
    @PutMapping(value = {"/{id}/suspend", "/{id}/ban"})
    @Auditable(menuName = "Terminal Management", action = "BAN", targetType = "TERMINAL", targetId = "#id")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Ban / Suspend Terminal", description = "Bans a terminal and sets its status to SUSPENDED.")
    public ResponseEntity<?> suspendTerminal(
            @PathVariable UUID id, 
            @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            terminalService.updateTerminalStatus(id, "SUSPENDED");
            return ResponseEntity.ok(Map.of("message", "Terminal banned successfully, status set to SUSPENDED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
