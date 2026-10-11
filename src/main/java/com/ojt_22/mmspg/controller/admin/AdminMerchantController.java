package com.ojt_22.mmspg.controller.admin;

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

import com.ojt_22.mmspg.dto.MerchantPendingDto;
import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.enums.MerchantStatus;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.MerchantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final MerchantService merchantService;
    private final MerchantRepository merchantRepository;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
    public ResponseEntity<?> getAllMerchants() {
        java.util.List<com.ojt_22.mmspg.dto.MerchantResponseDto> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }
    
    
    @PostMapping("/register")
    // 🔴 ဤနေရာကို ပြင်ဆင်လိုက်ပါသည် (ADMIN သို့မဟုတ် STAFF နှစ်ခုလုံးကို ခွင့်ပြုပါမည်)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')") 
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
    
    
 // ၁။ Edit Merchant Details အတွက်
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> updateMerchantDetails(
            @PathVariable UUID id, 
            @RequestBody MerchantPendingDto updateDto) {
            
        // Database ထဲမှ သက်ဆိုင်ရာ Merchant ကို ရှာမည်
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // 🔴 Data အသစ်ပါလာမှသာ (null မဟုတ်မှသာ) Database ကို အစားထိုးရန် ကာကွယ်ထားခြင်း
        if (updateDto.getBusinessName() != null && !updateDto.getBusinessName().trim().isEmpty()) {
            merchant.setBusinessName(updateDto.getBusinessName());
        }
        
        if (updateDto.getMerchantCode() != null && !updateDto.getMerchantCode().trim().isEmpty()) {
            merchant.setMerchantCode(updateDto.getMerchantCode()); 
        }
        
        // Database သို့ Save လုပ်မည်
        merchantRepository.save(merchant);
        
        return ResponseEntity.ok(Map.of("message", "Merchant updated successfully"));
    }
    
    
 // ၂။ Suspend Status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> updateMerchantStatus(
            @PathVariable UUID id, 
            @RequestParam String status) {
            
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        merchant.setStatus(MerchantStatus.valueOf(status.toUpperCase()));
        merchantRepository.save(merchant);
        
        return ResponseEntity.ok(Map.of("message", "Merchant status updated successfully"));
    }
    
}