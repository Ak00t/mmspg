package com.ojt_22.mmspg.service;

import java.util.List;
import com.ojt_22.mmspg.dto.MerchantFeeRequestDto;
import com.ojt_22.mmspg.dto.MerchantFeeResponseDto;

public interface MerchantFeeService {
    MerchantFeeResponseDto createMerchantFee(MerchantFeeRequestDto request);
    List<MerchantFeeResponseDto> getAllFeesWithMerchantNames();
}
