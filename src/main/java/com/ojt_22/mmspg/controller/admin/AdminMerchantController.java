package com.ojt_22.mmspg.controller.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.dto.MerchantPendingDto;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.MerchantService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    @Auditable(menuName = "Merchant Management", action = "REGISTER", targetType = "MERCHANT", targetId = "#result?.body?.get('merchantId')")
    // 🔴 ဤနေရာကို ပြင်ဆင်လိုက်ပါသည် (ADMIN သို့မဟုတ် STAFF နှစ်ခုလုံးကို ခွင့်ပြုပါမည်)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')") 
    public ResponseEntity<?> registerMerchant(@Valid @RequestBody MerchantRegistrationRequest request) {
        try {
            Merchant savedMerchant = merchantService.registerMerchant(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Merchant registered successfully");
            response.put("merchantId", savedMerchant.getId() != null ? savedMerchant.getId().toString() : "");
            response.put("merchantCode", savedMerchant.getMerchantCode());
            response.put("status", savedMerchant.getStatus() != null ? savedMerchant.getStatus().name() : "PENDING");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Merchant registration validation failure: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Merchant registration server error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to register merchant")
            );
        }
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Get Merchant Profile", description = "Retrieves full profile details of a merchant by ID.")
    public ResponseEntity<?> getMerchantProfile(@PathVariable UUID id) {
        try {
            MerchantProfileResponseDto profile = merchantService.getMerchantProfile(id);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Upload Merchant Profile Picture", description = "Uploads profile picture and updates profile image URL.")
    public ResponseEntity<?> uploadMerchantProfilePicture(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = merchantService.uploadMerchantProfilePicture(id, file);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture uploaded successfully",
                    "profileImageUrl", filePath
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
