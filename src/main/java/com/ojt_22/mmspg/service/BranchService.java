package com.ojt_22.mmspg.service;

import java.util.List;
import java.util.UUID;
import com.ojt_22.mmspg.dto.BranchRequestDto;
import com.ojt_22.mmspg.dto.BranchResponseDto;

public interface BranchService {
    BranchResponseDto createBranch(BranchRequestDto request);
    List<BranchResponseDto> getAllBranches(UUID merchantId);
    BranchResponseDto updateBranch(UUID branchId, BranchRequestDto request);
    void toggleBranchStatus(UUID branchId);
}
