package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.BranchRequestDto;
import com.ojt_22.mmspg.dto.BranchResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantBranch;
import com.ojt_22.mmspg.repository.BranchRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.BranchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchRequestDto request) {
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + request.getMerchantId()));

        if (branchRepository.existsByBranchCodeAndMerchantId(request.getBranchCode(), request.getMerchantId())) {
            throw new IllegalArgumentException("Branch code already exists for this merchant.");
        }

        MerchantBranch branch = new MerchantBranch();
        branch.setMerchant(merchant);
        branch.setBranchCode(request.getBranchCode());
        branch.setBranchName(request.getBranchName());
        branch.setCity(request.getCity());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setStatus("ACTIVE");
        branch.setUpdatedAt(LocalDateTime.now());

        MerchantBranch savedBranch = branchRepository.save(branch);
        return mapToResponseDto(savedBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getAllBranches(UUID merchantId) {
        List<MerchantBranch> branches;
        if (merchantId != null) {
            branches = branchRepository.findByMerchantId(merchantId);
        } else {
            branches = branchRepository.findAll();
        }

        return branches.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BranchResponseDto updateBranch(UUID branchId, BranchRequestDto request) {
        MerchantBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + branchId));

        // If merchant changes (rare, but let's support it or throw error)
        if (!branch.getMerchant().getId().equals(request.getMerchantId())) {
            Merchant merchant = merchantRepository.findById(request.getMerchantId())
                    .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + request.getMerchantId()));
            branch.setMerchant(merchant);
        }

        // If code changes, ensure uniqueness
        if (!branch.getBranchCode().equals(request.getBranchCode())) {
            if (branchRepository.existsByBranchCodeAndMerchantId(request.getBranchCode(), request.getMerchantId())) {
                throw new IllegalArgumentException("Branch code already exists for this merchant.");
            }
            branch.setBranchCode(request.getBranchCode());
        }

        branch.setBranchName(request.getBranchName());
        branch.setCity(request.getCity());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setUpdatedAt(LocalDateTime.now());

        MerchantBranch updatedBranch = branchRepository.save(branch);
        return mapToResponseDto(updatedBranch);
    }

    @Override
    @Transactional
    public void toggleBranchStatus(UUID branchId) {
        MerchantBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + branchId));

        if ("ACTIVE".equals(branch.getStatus())) {
            branch.setStatus("INACTIVE");
        } else {
            branch.setStatus("ACTIVE");
        }
        
        branch.setUpdatedAt(LocalDateTime.now());
        branchRepository.save(branch);
    }

    private BranchResponseDto mapToResponseDto(MerchantBranch branch) {
        BranchResponseDto dto = new BranchResponseDto();
        dto.setBranchId(branch.getId());
        dto.setMerchantId(branch.getMerchant().getId());
        dto.setMerchantName(branch.getMerchant().getBusinessName());
        dto.setBranchCode(branch.getBranchCode());
        dto.setBranchName(branch.getBranchName());
        dto.setCity(branch.getCity());
        dto.setAddress(branch.getAddress());
        dto.setPhone(branch.getPhone());
        dto.setStatus(branch.getStatus());
        dto.setCreatedAt(branch.getCreatedAt());
        return dto;
    }
}
