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
import com.ojt_22.mmspg.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MccServiceImpl implements MccService {

	private final MccCodeRepository mccCodeRepository;
	private final NotificationService notificationService;

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
		String trimmedCode = request.getMccCode() != null ? request.getMccCode().trim() : "";
		if (trimmedCode.isEmpty()) {
			throw new IllegalArgumentException("MCC Code is required");
		}
		if (mccCodeRepository.existsByMccCode(trimmedCode)) {
			throw new IllegalArgumentException("MCC code already exists: " + trimmedCode);
		}

		MccCode mcc = new MccCode();
		mcc.setMccCode(trimmedCode);
		mcc.setMccName(request.getMccName() != null ? request.getMccName().trim() : "");
		mcc.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
		mcc.setStatus(MccCodeStatus.ACTIVE);

		MccCode saved = mccCodeRepository.save(mcc);
		
		notificationService.sendNotificationToCurrentStaff(
				"MCC Added",
				"MCC code \"" + saved.getMccCode() + " - " + saved.getMccName() + "\" has been successfully created.",
				"SYSTEM"
		);
		return mapToResponseDto(saved);
	}

	private MccCodeResponseDto mapToResponseDto(MccCode mcc) {
		MccCodeResponseDto dto = new MccCodeResponseDto();
		dto.setId(mcc.getId());
		dto.setMccCode(mcc.getMccCode());
		dto.setMccName(mcc.getMccName());
		dto.setDescription(mcc.getDescription());
		dto.setStatus(mcc.getStatus() != null ? mcc.getStatus().name() : "ACTIVE");
		return dto;
	}

	@Override
	@Transactional
	public void deleteMccCode(Long id) {
		MccCode mcc = mccCodeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("MCC not found with ID: " + id));

		try {
			mccCodeRepository.deleteById(id);
			
			notificationService.sendNotificationToCurrentStaff(
					"MCC Deleted",
					"MCC code \"" + mcc.getMccCode() + " - " + mcc.getMccName() + "\" has been successfully deleted.",
					"SYSTEM"
			);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Cannot delete this MCC code because it is being used by existing merchants.");
		}
	}

	@Override
	@Transactional
	public MccCodeResponseDto updateMccCode(Long id, MccCodeRequestDto request) {
		MccCode mcc = mccCodeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("MCC not found with ID: " + id));

		if (request.getMccCode() != null && !request.getMccCode().trim().isEmpty()) {
			String trimmedCode = request.getMccCode().trim();
			if (!trimmedCode.equals(mcc.getMccCode()) && mccCodeRepository.existsByMccCode(trimmedCode)) {
				throw new IllegalArgumentException("MCC code already exists: " + trimmedCode);
			}
			mcc.setMccCode(trimmedCode);
		}

		if (request.getMccName() != null && !request.getMccName().trim().isEmpty()) {
			mcc.setMccName(request.getMccName().trim());
		}

		if (request.getDescription() != null) {
			mcc.setDescription(request.getDescription().trim());
		}

		MccCode updatedMcc = mccCodeRepository.save(mcc);

		notificationService.sendNotificationToCurrentStaff(
				"MCC Updated",
				"MCC code \"" + updatedMcc.getMccCode() + " - " + updatedMcc.getMccName() + "\" has been successfully updated.",
				"SYSTEM"
		);

		return mapToResponseDto(updatedMcc);
	}
}
