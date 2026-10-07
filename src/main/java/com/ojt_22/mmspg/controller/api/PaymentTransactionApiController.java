package com.ojt_22.mmspg.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.PaymentAuthorizeRequestDto;
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.dto.PaymentAuthorizeResponseDto;
import com.ojt_22.mmspg.dto.PaymentCheckoutInfoDto;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.dto.PaymentStatusResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;
import com.ojt_22.mmspg.service.PaymentTransactionService;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentTransactionApiController {

    private final PaymentTransactionService transactionService;
    private final AuthenticatedMerchantService authenticatedMerchantService;

    @PostMapping("/initiate")
    @Auditable(menuName = "Payments", action = "INITIATE", targetType = "PAYMENT_TRANSACTION", targetId = "#result?.body?.transactionReference")
    public ResponseEntity<PaymentInitiateResponseDto> initiateTransaction(
            @Parameter(
                name = "Idempotency-Key", 
                description = "Unique UUID or Key for request idempotency", 
                required = true, 
                in = ParameterIn.HEADER
            )
            @NotBlank(message = "Idempotency-Key header must not be blank")
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody PaymentInitiateRequestDto requestDto,
            Authentication authentication) {
        
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_API_CLIENT".equals(a.getAuthority()))) {
            UUID authMerchantId = UUID.fromString(authentication.getName());
            if (requestDto.getMerchantId() != null && !authMerchantId.equals(requestDto.getMerchantId())) {
                throw new IllegalArgumentException("Merchant ID does not match authenticated API client");
            }
            requestDto.setMerchantId(authMerchantId);
        } else {
            UUID authMerchantId = authenticatedMerchantService.resolveMerchantId();
            if (requestDto.getMerchantId() != null && !authMerchantId.equals(requestDto.getMerchantId())) {
                throw new IllegalArgumentException("Merchant ID does not match authenticated merchant");
            }
            requestDto.setMerchantId(authMerchantId);
        }

        return ResponseEntity.ok(transactionService.initiateTransaction(requestDto, idempotencyKey));
    }
    
    @PostMapping("/authorize")
    @Auditable(menuName = "Payments", action = "AUTHORIZE", targetType = "PAYMENT_TRANSACTION", targetId = "#result?.body?.transactionReference")
    public ResponseEntity<PaymentAuthorizeResponseDto> authorizeTransaction(@Valid @RequestBody PaymentAuthorizeRequestDto requestDto) {
        return ResponseEntity.ok(transactionService.authorizeTransaction(requestDto));
    }

    @GetMapping("/status/{transactionReference}")
    public ResponseEntity<PaymentStatusResponseDto> getTransactionStatus(@PathVariable String transactionReference) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(transactionReference));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<PaymentTransactionSummaryDto>> getMerchantTransactions(
            @PathVariable UUID merchantId,
            Authentication authentication) {
        authenticatedMerchantService.assertMerchantAccess(merchantId);
        return ResponseEntity.ok(transactionService.getMerchantTransactions(merchantId));
    }

    @GetMapping("/checkout-info/{token}")
    public ResponseEntity<PaymentCheckoutInfoDto> getCheckoutInfo(@PathVariable String token) {
        return ResponseEntity.ok(transactionService.getCheckoutInfoByToken(token));
    }
}
