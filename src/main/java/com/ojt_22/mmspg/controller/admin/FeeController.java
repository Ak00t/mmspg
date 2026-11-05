package com.ojt_22.mmspg.controller.admin;

import java.math.BigDecimal;
import java.util.List;
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

import com.ojt_22.mmspg.dto.FeeCalculationResponse;
import com.ojt_22.mmspg.dto.FeeConfigRequest;
import com.ojt_22.mmspg.dto.FeeConfigResponse;
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.service.FeeService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/fees") 
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
public class FeeController {

	private final FeeService feeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "List all Merchant Fee rules")
    public ResponseEntity<List<FeeConfigResponse>> getAllFees() {
        return ResponseEntity.ok(feeService.getAllFees()); 
    }

	@PostMapping
	@Auditable(menuName = "Fee Configuration", action = "CREATE", targetType = "FEE_CONFIG", targetId = "#result?.body?.feeId")
    @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FeeConfigResponse> createFeeConfig(@Valid @RequestBody FeeConfigRequest request) {
		FeeConfigResponse response = feeService.createFeeConfig(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{feeId}")
	@Auditable(menuName = "Fee Configuration", action = "UPDATE", targetType = "FEE_CONFIG", targetId = "#feeId")
    @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FeeConfigResponse> updateFeeConfig(@PathVariable UUID feeId,
			@Valid @RequestBody FeeConfigRequest request) {
		FeeConfigResponse response = feeService.updateFeeConfig(feeId, request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
	public ResponseEntity<FeeCalculationResponse> calculateFee(@RequestParam UUID feeId,
			@RequestParam BigDecimal amount) {
		FeeCalculationResponse response = feeService.calculateFee(feeId, amount);
		return ResponseEntity.ok(response);
	}
}
