package com.ojt_22.mmspg.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.PaymentAuthorizeRequestDto;
import com.ojt_22.mmspg.dto.PaymentAuthorizeResponseDto;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.dto.PaymentStatusResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import com.ojt_22.mmspg.service.PaymentTransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentTransactionApiController {

    private final PaymentTransactionService transactionService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponseDto> initiateTransaction(@RequestBody PaymentInitiateRequestDto requestDto) {
        return ResponseEntity.ok(transactionService.initiateTransaction(requestDto));
    }
    
    @PostMapping("/authorize")
    public ResponseEntity<PaymentAuthorizeResponseDto> authorizeTransaction(@Valid @RequestBody PaymentAuthorizeRequestDto requestDto) {
        return ResponseEntity.ok(transactionService.authorizeTransaction(requestDto));
    }

    @GetMapping("/status/{transactionReference}")
    public ResponseEntity<PaymentStatusResponseDto> getTransactionStatus(@PathVariable String transactionReference) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionReference));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<PaymentTransactionSummaryDto>> getMerchantTransactions(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(transactionService.getMerchantTransactions(merchantId));
    }
}