package com.ojt_22.mmspg.controller.admin;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.ojt_22.mmspg.service.FeeService;

@RestController
@RequestMapping("/api/admin/fees")
public class FeeController {
	
	private final FeeService feeService;
	
	public FeeController(FeeService feeService) {
		this.feeService = feeService;
	}
	
	@PostMapping
	public ResponseEntity<FeeConfigResponse> createFeeConfig(@RequestBody FeeConfigRequest request){
		
		FeeConfigResponse response = feeService.createFeeConfig(request);
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}
	
	@PutMapping("/{feeId}")
	public ResponseEntity<FeeConfigResponse> updateFeeConfig(
	        @PathVariable UUID feeId,
	        @RequestBody FeeConfigRequest request) {

	    FeeConfigResponse response =
	            feeService.updateFeeConfig(feeId, request);

	    return ResponseEntity.ok(response);
	}
	
	@PostMapping("/calculate")
	public ResponseEntity<FeeCalculationResponse> calculateFee(
	        @RequestParam UUID feeId,
	        @RequestParam BigDecimal amount) {

	    FeeCalculationResponse response =
	            feeService.calculateFee(feeId, amount);

	    return ResponseEntity.ok(response);
	}
}
