package com.ojt_22.mmspg.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ojt_22.mmspg.dto.MerchantPendingDto;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.entity.MccCode;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.enums.MerchantStatus;
import com.ojt_22.mmspg.repository.MccCodeRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.service.CodeGeneratorService;
import com.ojt_22.mmspg.service.MerchantService;
import com.ojt_22.mmspg.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

	private final MerchantRepository merchantRepository;
	private final MccCodeRepository mccCodeRepository;
	private final PasswordEncoder passwordEncoder;
	private final NotificationService notificationService;
	private final CodeGeneratorService codeGeneratorService;

	@Override
	@Transactional
	public Merchant registerMerchant(MerchantRegistrationRequest request) {

		if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty()) {
			throw new IllegalArgumentException("Business name is required");
		}
		if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required");
		}
		if (request.getRawPassword() == null || request.getRawPassword().trim().length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters");
		}

		String trimmedCode;
		if (request.getMerchantCode() == null || request.getMerchantCode().trim().isEmpty()) {
			trimmedCode = codeGeneratorService.generateMerchantCode();
		} else {
			trimmedCode = request.getMerchantCode().trim();
			if (merchantRepository.existsByMerchantCode(trimmedCode)) {
				throw new IllegalArgumentException("Merchant code already exists: " + trimmedCode);
			}
		}
		String trimmedEmail = request.getEmail().trim().toLowerCase();

		if (merchantRepository.existsByEmail(trimmedEmail)) {
			throw new IllegalArgumentException("Email is already in use: " + trimmedEmail);
		}

		MccCode mccCode = null;
		if (request.getMccId() != null) {
			mccCode = mccCodeRepository.findById(request.getMccId()).orElse(null);
		}
		if (mccCode == null) {
			java.util.List<MccCode> allMccs = mccCodeRepository.findAll();
			if (!allMccs.isEmpty()) {
				mccCode = allMccs.get(0);
			} else {
				MccCode defaultMcc = new MccCode();
				defaultMcc.setMccCode("5411");
				defaultMcc.setMccName("General Merchant");
				defaultMcc.setDescription("Default Merchant Category");
				defaultMcc.setStatus(com.ojt_22.mmspg.enums.MccCodeStatus.ACTIVE);
				mccCode = mccCodeRepository.save(defaultMcc);
			}
		}

		Merchant merchant = new Merchant();
		merchant.setMerchantCode(trimmedCode);
		merchant.setBusinessName(request.getBusinessName().trim());
		merchant.setBusinessRegistrationNo(trimmedCode);
		merchant.setEmail(trimmedEmail);
		merchant.setMccCode(mccCode);
		merchant.setSettlementAccountNo(
				request.getSettlementAccountNo() != null && !request.getSettlementAccountNo().trim().isEmpty()
						? request.getSettlementAccountNo().trim()
						: "SETTLE-" + System.currentTimeMillis()
		);
		merchant.setContactName(request.getContactName());
		merchant.setPhone(request.getPhone());
		merchant.setAddress(request.getAddress());

		// Hash the password
		merchant.setPasswordHash(passwordEncoder.encode(request.getRawPassword().trim()));

		// Default status
		merchant.setStatus(MerchantStatus.PENDING);
		merchant.setUpdatedAt(LocalDateTime.now());

		Merchant savedMerchant = merchantRepository.save(merchant);

		// Trigger notification on success safely
		try {
			notificationService.sendNotificationToCurrentStaff(
					"New Merchant Added",
					"Merchant \"" + savedMerchant.getBusinessName() + "\" (Code: " + savedMerchant.getMerchantCode() + ") has been successfully registered with PENDING status.",
					"MERCHANT"
			);
		} catch (Exception ex) {
			log.warn("Failed to dispatch notification for merchant registration: {}", ex.getMessage());
		}

		return savedMerchant;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<MerchantPendingDto> getPendingMerchants(int page, int size) {
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page, size,
				org.springframework.data.domain.Sort.by("createdAt")
						.descending());
		return merchantRepository.findByStatus(MerchantStatus.PENDING, pageable)
				.map(merchant -> {
					MerchantPendingDto dto = new MerchantPendingDto();
					dto.setMerchantId(merchant.getId());
					dto.setMerchantCode(merchant.getMerchantCode());
					dto.setBusinessName(merchant.getBusinessName());
					dto.setEmail(merchant.getEmail());
					dto.setBusinessType(merchant.getMccCode() != null ? merchant.getMccCode()
							.getMccName() : "N/A");
					dto.setCreatedAt(merchant.getCreatedAt());
					dto.setStatus(merchant.getStatus()
							.name());
					return dto;
				});
	}

	@Override
	@Transactional
	public void approveMerchant(java.util.UUID merchantId) {
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));

		if (merchant.getStatus() != MerchantStatus.PENDING) {
			throw new IllegalStateException(
					"Merchant is not in PENDING status. Current status: " + merchant.getStatus());
		}

		merchant.setStatus(MerchantStatus.ACTIVE);
		merchant.setApprovedAt(LocalDateTime.now());
		merchant.setUpdatedAt(LocalDateTime.now());
		merchantRepository.save(merchant);

		// Trigger notification to the currently logged-in Admin
		notificationService.sendNotificationToCurrentStaff("Merchant Approved", "You approved merchant \""
				+ merchant.getBusinessName() + "\" (Code: " + merchant.getMerchantCode() + ").", "MERCHANT");

		// Broadcast to all other staff/auditors
		notificationService.sendNotificationToAllStaff("Merchant Approved", "Merchant \"" + merchant.getBusinessName()
				+ "\" (Code: " + merchant.getMerchantCode() + ") has been approved successfully.", "MERCHANT");
	}

	@Override
	@Transactional(readOnly = true)
	public java.util.List<com.ojt_22.mmspg.dto.MerchantResponseDto> getAllMerchants() {
		java.util.List<Merchant> merchants = merchantRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

		return merchants.stream()
				.map(merchant -> {
					com.ojt_22.mmspg.dto.MerchantResponseDto dto = new com.ojt_22.mmspg.dto.MerchantResponseDto();

					dto.setMerchantId(merchant.getId());
					dto.setMerchantCode(merchant.getMerchantCode());
					// ⚠️ သင့် Entity တွင် businessName ဟုပေးထားသဖြင့် React လိုချင်သော merchantName
					// ထဲသို့ ထည့်ပေးပါသည်
					dto.setMerchantName(merchant.getBusinessName());
					dto.setEmail(merchant.getEmail());
					dto.setCreatedAt(merchant.getCreatedAt());

					// ⚠️ သင့် Entity တွင် riskLevel မရှိသေးပါက လောလောဆယ် "LOW" ဟု ပုံသေထားပေးပါမည်
					dto.setRiskLevel("LOW");

					dto.setStatus(merchant.getStatus() != null ? merchant.getStatus()
							.name() : "PENDING");

					return dto;
				})
				.collect(java.util.stream.Collectors.toList());
	}

	@Override
	@Transactional
	public void rejectMerchant(java.util.UUID merchantId, String reason) {
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));

		if (merchant.getStatus() != MerchantStatus.PENDING) {
			throw new IllegalStateException(
					"Merchant is not in PENDING status. Current status: " + merchant.getStatus());
		}

		merchant.setStatus(MerchantStatus.REJECTED);
		merchant.setUpdatedAt(LocalDateTime.now());
		merchantRepository.save(merchant);

		// Trigger notification to the currently logged-in Admin
		notificationService.sendNotificationToCurrentStaff("Merchant Rejected", "You rejected merchant \""
				+ merchant.getBusinessName() + "\" (Code: " + merchant.getMerchantCode() + "). Reason: " + reason,
				"MERCHANT");

		// Broadcast notification to other staff/admins
		notificationService.sendNotificationToAllStaff("Merchant Rejected", "Merchant \"" + merchant.getBusinessName()
				+ "\" (Code: " + merchant.getMerchantCode() + ") has been rejected. Reason: " + reason, "MERCHANT");
	}

	@Override
	@Transactional(readOnly = true)
	public MerchantProfileResponseDto getMerchantProfile(java.util.UUID merchantId) {
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));

		return MerchantProfileResponseDto.builder()
				.merchantId(merchant.getId())
				.merchantCode(merchant.getMerchantCode())
				.businessName(merchant.getBusinessName())
				.businessRegistrationNo(merchant.getBusinessRegistrationNo())
				.settlementAccountNo(merchant.getSettlementAccountNo())
				.contactName(merchant.getContactName())
				.email(merchant.getEmail())
				.phone(merchant.getPhone())
				.address(merchant.getAddress())
				.status(merchant.getStatus() != null ? merchant.getStatus()
						.name() : null)
				.profileImageUrl(merchant.getProfileImageUrl())
				.businessType(merchant.getMccCode() != null ? merchant.getMccCode()
						.getMccName() : null)
				.createdAt(merchant.getCreatedAt())
				.build();
	}

	@Override
	@Transactional
	public String uploadMerchantProfilePicture(java.util.UUID merchantId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Profile image file cannot be empty");
		}

		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));

		try {
			Path uploadDir = Paths.get("uploads", "merchants");
			if (!Files.exists(uploadDir)) {
				Files.createDirectories(uploadDir);
			}

			String originalFilename = file.getOriginalFilename();
			String extension = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."))
						.toLowerCase();
			}

			String fileName = merchantId + "_" + System.currentTimeMillis() + extension;
			Path targetPath = uploadDir.resolve(fileName);

			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

			String fileUrl = "/uploads/merchants/" + fileName;
			merchant.setProfileImageUrl(fileUrl);
			merchant.setUpdatedAt(LocalDateTime.now());
			merchantRepository.save(merchant);

			return fileUrl;
		} catch (IOException e) {
			throw new RuntimeException("Failed to upload profile picture: " + e.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public void updateMerchant(UUID merchantId, String businessName, String merchantCode) {
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));

		merchant.setBusinessName(businessName);
		merchant.setMerchantCode(merchantCode);
		merchant.setUpdatedAt(LocalDateTime.now());

		merchantRepository.save(merchant);

		// Trigger Notification on Edit
		notificationService.sendNotificationToCurrentStaff("Merchant Updated",
				"Details for merchant \"" + merchant.getBusinessName() + "\" have been successfully updated.",
				"MERCHANT");
	}

	@Override
	@Transactional
	public void updateMerchantStatus(UUID merchantId, String status, String reason) {
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + merchantId));

		try {
			merchant.setStatus(MerchantStatus.valueOf(status.toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid status provided: " + status);
		}

		merchant.setUpdatedAt(LocalDateTime.now());
		merchantRepository.save(merchant);

		// Trigger Notification on Status Change (Suspend)
		notificationService.sendNotificationToCurrentStaff("Merchant Status Updated", "Merchant \""
				+ merchant.getBusinessName() + "\" status was changed to " + status + ". Reason: " + reason,
				"MERCHANT");
	}

}
