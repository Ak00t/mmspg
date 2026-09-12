package com.ojt_22.mmspg.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MccCodeRequestDto;
import com.ojt_22.mmspg.dto.MccCodeResponseDto;
import com.ojt_22.mmspg.service.MccService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/mcc")
@RequiredArgsConstructor
@Tag(name = "Admin MCC Configuration", description = "Endpoints for managing MCC Dictionary")
public class AdminMccController {

    private final MccService mccService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all MCC codes", description = "Retrieves a list of all MCC codes and their descriptions.")
    public ResponseEntity<List<MccCodeResponseDto>> getAllMccCodes() {
        return ResponseEntity.ok(mccService.getAllMccCodes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new MCC", description = "Creates a new MCC code in the dictionary.")
    public ResponseEntity<MccCodeResponseDto> addMccCode(@Valid @RequestBody MccCodeRequestDto request) {
        MccCodeResponseDto response = mccService.addMccCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
