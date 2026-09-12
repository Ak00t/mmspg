package com.ojt_22.mmspg.controller.admin;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.service.MerchantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    // 🔴 ဤနေရာကို ပြင်ဆင်လိုက်ပါသည် (ADMIN သို့မဟုတ် STAFF နှစ်ခုလုံးကို ခွင့်ပြုပါမည်)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") 
    public ResponseEntity<?> registerMerchant(@Valid @RequestBody MerchantRegistrationRequest request) {
        try {
            Merchant savedMerchant = merchantService.registerMerchant(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Merchant registered successfully",
                            "merchantId", savedMerchant.getId(),
                            "merchantCode", savedMerchant.getMerchantCode(),
                            "status", savedMerchant.getStatus()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}