package com.ojt_22.mmspg.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.MccCodeRequestDto;
import com.ojt_22.mmspg.dto.MccCodeResponseDto;
import com.ojt_22.mmspg.entity.MccCode;
import com.ojt_22.mmspg.enums.MccCodeStatus;
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
		return mccCodeRepository.findAll()
				.stream()
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
		mcc.setStatus(MccCodeStatus.ACTIVE);

		MccCode saved = mccCodeRepository.save(mcc);
		return mapToResponseDto(saved);
	}

	private MccCodeResponseDto mapToResponseDto(MccCode mcc) {
		MccCodeResponseDto dto = new MccCodeResponseDto();
		dto.setId(mcc.getId());
		dto.setMccCode(mcc.getMccCode());
		dto.setMccName(mcc.getMccName());
		dto.setDescription(mcc.getDescription());
		dto.setStatus(mcc.getStatus()
				.name());
		return dto;
	}
	
	
	@Override
    @Transactional
    public void deleteMccCode(Long id) {
        // Database Repository က Integer တောင်းသဖြင့် id.intValue() ဟု ပြောင်းပေးပါသည်
        if (!mccCodeRepository.existsById(id.intValue())) {
            throw new IllegalArgumentException("MCC not found with ID: " + id);
        }
        
        try {
            mccCodeRepository.deleteById(id.intValue());
        } catch (Exception e) {
            // အကယ်၍ Merchant နှင့် ချိတ်ဆက်ထားသဖြင့် ဖျက်မရပါက Error Message ပြန်ပေးမည်
            throw new IllegalArgumentException("Cannot delete this MCC code because it is being used by existing merchants.");
        }
    }
	
	@Override
    @Transactional
    public MccCodeResponseDto updateMccCode(Long id, MccCodeRequestDto request) { // Parameter သည် Long အတိုင်း ဆက်ရှိနေမည်
        
        // 🔴 id.intValue() ဟု ပြောင်းရေးလိုက်ပါ
        MccCode mcc = mccCodeRepository.findById(id.intValue())
                .orElseThrow(() -> new IllegalArgumentException("MCC not found with ID: " + id));

        // Frontend မှ Data ပါလာမှသာ (null မဟုတ်မှသာ) အစားထိုးမည်
        if (request.getMccCode() != null && !request.getMccCode().trim().isEmpty()) {
            mcc.setMccCode(request.getMccCode());
        }
        
        if (request.getMccName() != null && !request.getMccName().trim().isEmpty()) {
            mcc.setMccName(request.getMccName());
        }
        
        if (request.getDescription() != null) {
            mcc.setDescription(request.getDescription());
        }

        MccCode updatedMcc = mccCodeRepository.save(mcc);
        
        return mapToResponseDto(updatedMcc); 
    }
	
	
	
}
