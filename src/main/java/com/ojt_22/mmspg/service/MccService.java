package com.ojt_22.mmspg.service;

import java.util.List;
import com.ojt_22.mmspg.dto.MccCodeRequestDto;
import com.ojt_22.mmspg.dto.MccCodeResponseDto;

public interface MccService {
    List<MccCodeResponseDto> getAllMccCodes();
    MccCodeResponseDto addMccCode(MccCodeRequestDto request);
}
