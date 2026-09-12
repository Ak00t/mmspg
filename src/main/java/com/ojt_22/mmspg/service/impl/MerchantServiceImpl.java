package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.entity.MccCode;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MccCodeRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.MerchantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MccCodeRepository mccCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Merchant registerMerchant(MerchantRegistrationRequest request) {
        
        if (merchantRepository.existsByMerchantCode(request.getMerchantCode())) {
            throw new IllegalArgumentException("Merchant code already exists");
        }

        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        MccCode mccCode = mccCodeRepository.findById(request.getMccId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid MCC ID provided"));

        Merchant merchant = new Merchant();
        merchant.setMerchantCode(request.getMerchantCode());
        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());
        merchant.setMccCode(mccCode);
        merchant.setSettlementAccountNo(request.getSettlementAccountNo());
        merchant.setContactName(request.getContactName());
        merchant.setPhone(request.getPhone());
        merchant.setAddress(request.getAddress());
        
        // Hash the password
        merchant.setPasswordHash(passwordEncoder.encode(request.getRawPassword()));
        
        // Default status
        merchant.setStatus("PENDING");
        
        // updatedAt must be set manually if there's no @UpdateTimestamp
        merchant.setUpdatedAt(LocalDateTime.now());

        return merchantRepository.save(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.ojt_22.mmspg.dto.MerchantPendingDto> getPendingMerchants(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        return merchantRepository.findByStatus("PENDING", pageable).map(merchant -> {
            com.ojt_22.mmspg.dto.MerchantPendingDto dto = new com.ojt_22.mmspg.dto.MerchantPendingDto();
            dto.setMerchantId(merchant.getId());
            dto.setMerchantCode(merchant.getMerchantCode());
            dto.setBusinessName(merchant.getBusinessName());
            dto.setEmail(merchant.getEmail());
            dto.setBusinessType(merchant.getMccCode() != null ? merchant.getMccCode().getMccName() : "N/A");
            dto.setCreatedAt(merchant.getCreatedAt());
            dto.setStatus(merchant.getStatus());
            return dto;
        });
    }

    @Override
    @Transactional
    public void approveMerchant(java.util.UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));
        
        if (!"PENDING".equals(merchant.getStatus())) {
            throw new IllegalStateException("Merchant is not in PENDING status. Current status: " + merchant.getStatus());
        }

        merchant.setStatus("ACTIVE");
        merchant.setApprovedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);
    }

    @Override
    @Transactional
    public void rejectMerchant(java.util.UUID merchantId, String reason) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));
        
        if (!"PENDING".equals(merchant.getStatus())) {
            throw new IllegalStateException("Merchant is not in PENDING status. Current status: " + merchant.getStatus());
        }

        merchant.setStatus("REJECTED");
        merchant.setUpdatedAt(LocalDateTime.now());
        // Since there is no 'rejectionReason' field in Merchant entity based on previous exploration, we just update status. 
        // If there was a log table, we would insert the reason there.
        merchantRepository.save(merchant);
    }
}
