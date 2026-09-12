package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MerchantFeeRequestDto;
import com.ojt_22.mmspg.dto.MerchantFeeResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantFee;
import com.ojt_22.mmspg.repository.MerchantFeeRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.MerchantFeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantFeeServiceImpl implements MerchantFeeService {

    private final MerchantFeeRepository merchantFeeRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public MerchantFeeResponseDto createMerchantFee(MerchantFeeRequestDto request) {
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + request.getMerchantId()));

        MerchantFee fee = new MerchantFee();
        fee.setMerchant(merchant);
        
        String feeType = request.getFeeType().toUpperCase();
        if (!feeType.equals("PERCENTAGE") && !feeType.equals("FLAT") && !feeType.equals("MIXED")) {
            throw new IllegalArgumentException("Invalid feeType. Must be PERCENTAGE, FLAT, or MIXED.");
        }
        fee.setFeeType(feeType);
        
        fee.setPercentageRate(request.getFeePercentage());
        fee.setFlatFee(request.getFlatAmount());
        
        fee.setEffectiveFrom(LocalDateTime.now());
        fee.setStatus("ACTIVE");
        fee.setCreatedAt(LocalDateTime.now());
        
        MerchantFee savedFee = merchantFeeRepository.save(fee);
        return mapToResponseDto(savedFee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantFeeResponseDto> getAllFeesWithMerchantNames() {
        return merchantFeeRepository.findAllWithMerchant().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private MerchantFeeResponseDto mapToResponseDto(MerchantFee fee) {
        MerchantFeeResponseDto dto = new MerchantFeeResponseDto();
        dto.setFeeId(fee.getId());
        dto.setMerchantId(fee.getMerchant().getId());
        dto.setMerchantName(fee.getMerchant().getBusinessName());
        dto.setFeeType(fee.getFeeType());
        dto.setFeePercentage(fee.getPercentageRate());
        dto.setFlatAmount(fee.getFlatFee());
        dto.setStatus(fee.getStatus());
        dto.setEffectiveFrom(fee.getEffectiveFrom());
        dto.setEffectiveTo(fee.getEffectiveTo());
        dto.setCreatedAt(fee.getCreatedAt());
        return dto;
    }
}
