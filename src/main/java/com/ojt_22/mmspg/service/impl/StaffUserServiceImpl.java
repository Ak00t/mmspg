package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.UserProfileResponseDto;
import com.ojt_22.mmspg.dto.StaffUserRegisterRequest;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.enums.StaffUserRole;
import com.ojt_22.mmspg.enums.StaffUserStatus;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.service.NotificationService;
import com.ojt_22.mmspg.service.StaffUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffUserServiceImpl implements StaffUserService {

	private final StaffUserRepository staffUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public StaffUser registerStaff(StaffUserRegisterRequest request) {

//		if (staffUserRepository.existsByUsername(request.getUsername())) {
//			throw new IllegalArgumentException("Username is already taken");
//		}

		if (staffUserRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email is already in use");
		}

		StaffUser staffUser = new StaffUser();
		staffUser.setUsername(request.getEmail());
		staffUser.setEmail(request.getEmail());
		staffUser.setFullName(request.getFullName());
		staffUser.setRole(StaffUserRole.valueOf(request.getRole()));
		staffUser.setStatus(StaffUserStatus.ACTIVE);

		// Hash the password
		staffUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		return staffUserRepository.save(staffUser);
	}

	@Override
	public List<StaffUser> getAllStaff() {
		return staffUserRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<com.ojt_22.mmspg.dto.StaffResponseDto> getAllStaffUsersDto() {
		return staffUserRepository.findAll()
				.stream()
				.map(this::mapToResponseDto)
				.toList();
	}

	@Override
	@Transactional
	public com.ojt_22.mmspg.dto.StaffResponseDto createStaffUser(com.ojt_22.mmspg.dto.StaffRequestDto request) {
		if (staffUserRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email is already in use");
		}

		StaffUser staffUser = new StaffUser();
		staffUser.setUsername(request.getEmail()); // Using email as username since UI doesn't provide it
		staffUser.setEmail(request.getEmail());
		staffUser.setFullName(request.getName());

		String role = request.getRole()
				.toUpperCase();
		if (!role.equals("ADMIN") && !role.equals("AUDITOR") && !role.equals("SUPPORT")) {
			throw new IllegalArgumentException("Invalid role. Must be ADMIN, AUDITOR, or SUPPORT.");
		}
		staffUser.setRole(StaffUserRole.valueOf(role));
		staffUser.setStatus(StaffUserStatus.ACTIVE);
		staffUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		staffUser.setUpdatedAt(LocalDateTime.now());

		StaffUser saved = staffUserRepository.save(staffUser);

		// Trigger notification on success to the logged-in Admin
		notificationService.sendNotificationToCurrentStaff(
				"New Staff Member Added",
				"Staff account for \"" + saved.getFullName() + "\" (" + saved.getEmail() + ") with role " + saved.getRole() + " was successfully created.",
				"SYSTEM"
		);

		return mapToResponseDto(saved);
	}

	@Override
	@Transactional
	public void toggleStaffStatus(java.util.UUID staffId) {
		StaffUser staffUser = staffUserRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffId));

		if ("ACTIVE".equals(staffUser.getStatus()
				.name())) {
			staffUser.setStatus(StaffUserStatus.DISABLED);
		} else {
			staffUser.setStatus(StaffUserStatus.ACTIVE);
		}
		staffUser.setUpdatedAt(LocalDateTime.now());
		staffUserRepository.save(staffUser);

		// Trigger notification on status change to the logged-in Admin
		notificationService.sendNotificationToCurrentStaff(
				"Staff Status Changed",
				"Staff account \"" + staffUser.getFullName() + "\" status was changed to " + staffUser.getStatus() + ".",
				"SYSTEM"
		);
	}

	private com.ojt_22.mmspg.dto.StaffResponseDto mapToResponseDto(StaffUser user) {
		com.ojt_22.mmspg.dto.StaffResponseDto dto = new com.ojt_22.mmspg.dto.StaffResponseDto();
		dto.setId(user.getId());
		dto.setName(user.getFullName());
		dto.setEmail(user.getEmail());
		dto.setRole(user.getRole()
				.name());
		dto.setStatus(user.getStatus()
				.name());
		return dto;
	}
	
	
	
	@Override
    @Transactional
    public void updateStaffUser(UUID id, String fullName, String email, String role) {
        StaffUser user = staffUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + id));
        
        user.setFullName(fullName);
        user.setEmail(email);
        
        String upperRole = role.toUpperCase();
        if (!upperRole.equals("ADMIN") && !upperRole.equals("AUDITOR") && !upperRole.equals("SUPPORT")) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, AUDITOR, or SUPPORT.");
        }
        user.setRole(StaffUserRole.valueOf(upperRole)); 
        user.setUpdatedAt(LocalDateTime.now());
        
        staffUserRepository.save(user);

        // Trigger notification to the logged-in Admin
        notificationService.sendNotificationToCurrentStaff(
                "Staff Profile Updated",
                "Staff user \"" + user.getFullName() + "\" (" + user.getEmail() + ") details were updated.",
                "SYSTEM"
        );
    }

    // =========================================================================
    // 🔴 ဤသည်မှာ Compilation Error (အနီရောင်) ပျောက်သွားစေရန် ထပ်ထည့်ပေးထားသော Delete အပိုင်းဖြစ်ပါသည်
    // =========================================================================
    @Override
    @Transactional
    public void deleteStaffUser(UUID id) {
        StaffUser staff = staffUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + id));

        staffUserRepository.deleteById(id);

        // Trigger notification to the logged-in Admin
        notificationService.sendNotificationToCurrentStaff(
                "Staff Member Deleted",
                "Staff member \"" + staff.getFullName() + "\" (" + staff.getEmail() + ") was removed from the system.",
                "SYSTEM"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getStaffProfile(UUID staffId) {
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffId));

        return UserProfileResponseDto.builder()
                .id(staff.getId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .role(staff.getRole() != null ? staff.getRole().name() : null)
                .status(staff.getStatus() != null ? staff.getStatus().name() : null)
                .profileImageUrl(staff.getProfileImageUrl())
                .build();
    }

    @Override
    @Transactional
    public void changeStaffPassword(UUID staffId, ChangePasswordRequestDto request) {
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password");
        }

        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staff.setUpdatedAt(LocalDateTime.now());
        staffUserRepository.save(staff);

        // Send Security Notification
        notificationService.sendNotification(
                staffId,
                "Password Updated",
                "Your account password was successfully updated. If you did not make this change, please contact an administrator immediately.",
                "SECURITY"
        );
    }
    
    
    @Override
    @Transactional
    public String uploadProfilePicture(UUID id, org.springframework.web.multipart.MultipartFile file) {
        StaffUser user = staffUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + id));

        try {
            // ၁။ ပုံသိမ်းမည့် ဖိုင်တွဲ (Folder) တည်ဆောက်ခြင်း
            String uploadDir = "uploads/profiles/";
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // ၂။ နာမည်မထပ်စေရန် ပုံနာမည်ကို UUID ဖြင့် ပြောင်းလဲခြင်း
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;

            // ၃။ ပုံကို Hard Disk ပေါ်သို့ သိမ်းဆည်းခြင်း
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + newFilename);
            java.nio.file.Files.copy(
                    file.getInputStream(), 
                    filePath, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // ၄။ Database ထဲရှိ profileImageUrl ကော်လံကို Update လုပ်ခြင်း
            String fileUrl = "/uploads/profiles/" + newFilename;
            user.setProfileImageUrl(fileUrl);
            user.setUpdatedAt(LocalDateTime.now());
            staffUserRepository.save(user);

            // Send Profile Update Notification
            notificationService.sendNotification(
                    id,
                    "Profile Updated",
                    "Your profile picture has been successfully updated.",
                    "SYSTEM"
            );

            return fileUrl;

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to store profile picture", e);
        }
    }
}
