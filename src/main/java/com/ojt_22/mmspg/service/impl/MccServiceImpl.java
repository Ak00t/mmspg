package com.ojt_22.mmspg.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MccCodeRequestDto;
import com.ojt_22.mmspg.dto.MccCodeResponseDto;
import com.ojt_22.mmspg.entity.MccCode;
import com.ojt_22.mmspg.repository.MccCodeRepository;
import com.ojt_22.mmspg.service.MccService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MccServiceImpl implements MccService {

    private final MccCodeRepository mccCodeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MccCodeResponseDto> getAllMccCodes() {
        return mccCodeRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MccCodeResponseDto addMccCode(MccCodeRequestDto request) {
        MccCode mcc = new MccCode();
        mcc.setMccCode(request.getMccCode());
        mcc.setMccName(request.getMccName());
        mcc.setDescription(request.getDescription());
        mcc.setStatus("ACTIVE");
        
        MccCode saved = mccCodeRepository.save(mcc);
        return mapToResponseDto(saved);
    }

    private MccCodeResponseDto mapToResponseDto(MccCode mcc) {
        MccCodeResponseDto dto = new MccCodeResponseDto();
        dto.setId(mcc.getId());
        dto.setMccCode(mcc.getMccCode());
        dto.setMccName(mcc.getMccName());
        dto.setDescription(mcc.getDescription());
        dto.setStatus(mcc.getStatus());
        return dto;
    }
}
