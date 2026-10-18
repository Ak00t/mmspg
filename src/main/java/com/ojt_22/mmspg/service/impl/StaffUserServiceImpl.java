package com.ojt_22.mmspg.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.StaffRequestDto;
import com.ojt_22.mmspg.dto.StaffResponseDto;
import com.ojt_22.mmspg.dto.StaffUserRegisterRequest;
import com.ojt_22.mmspg.dto.UserProfileResponseDto;
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
        if (staffUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        StaffUser staffUser = new StaffUser();
        String username = request.getUsername() != null && !request.getUsername().isBlank()
                ? request.getUsername()
                : request.getEmail();
        staffUser.setUsername(username);
        staffUser.setEmail(request.getEmail());
        staffUser.setFullName(request.getFullName());

        String role = request.getRole().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("AUDITOR") && !role.equals("SUPPORT") && !role.equals("STAFF")) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, AUDITOR, SUPPORT, or STAFF.");
        }
        staffUser.setRole(StaffUserRole.valueOf(role));
        staffUser.setStatus(StaffUserStatus.ACTIVE);

        staffUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return staffUserRepository.save(staffUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffUser> getAllStaff() {
        return staffUserRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponseDto> getAllStaffUsersDto() {
        return staffUserRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public StaffResponseDto createStaffUser(StaffRequestDto request) {
        if (staffUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        StaffUser staffUser = new StaffUser();
        staffUser.setUsername(request.getEmail());
        staffUser.setEmail(request.getEmail());
        staffUser.setFullName(request.getName());

        String role = request.getRole().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("AUDITOR") && !role.equals("SUPPORT") && !role.equals("STAFF")) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, AUDITOR, SUPPORT, or STAFF.");
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
    public void toggleStaffStatus(UUID staffId) {
        StaffUser staffUser = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffId));

        if ("ACTIVE".equalsIgnoreCase(staffUser.getStatus().name())) {
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

    @Override
    @Transactional
    public void updateStaffStatus(UUID staffId, String status) {
        StaffUser staffUser = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffId));

        try {
            staffUser.setStatus(StaffUserStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            if ("INACTIVE".equalsIgnoreCase(status)) {
                staffUser.setStatus(StaffUserStatus.DISABLED);
            } else {
                throw new IllegalArgumentException("Invalid status provided: " + status);
            }
        }
        staffUser.setUpdatedAt(LocalDateTime.now());
        staffUserRepository.save(staffUser);

        notificationService.sendNotificationToCurrentStaff(
                "Staff Status Changed",
                "Staff account \"" + staffUser.getFullName() + "\" status was changed to " + staffUser.getStatus() + ".",
                "SYSTEM"
        );
    }

    @Override
    @Transactional
    public void reactivateStaff(UUID staffId) {
        updateStaffStatus(staffId, "ACTIVE");
    }

    @Override
    @Transactional
    public void updateStaffUser(UUID id, String fullName, String email, String role) {
        StaffUser user = staffUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + id));
        
        user.setFullName(fullName);
        user.setEmail(email);
        
        String upperRole = role.toUpperCase();
        if (!upperRole.equals("ADMIN") && !upperRole.equals("AUDITOR") && !upperRole.equals("SUPPORT") && !upperRole.equals("STAFF")) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, AUDITOR, SUPPORT, or STAFF.");
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
    public String uploadProfilePicture(UUID id, MultipartFile file) {
        StaffUser user = staffUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + id));

        try {
            String uploadDir = "uploads/profiles/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(uploadDir + newFilename);
            Files.copy(
                    file.getInputStream(), 
                    filePath, 
                    StandardCopyOption.REPLACE_EXISTING
            );

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

        } catch (IOException e) {
            throw new RuntimeException("Failed to store profile picture", e);
        }
    }

    private StaffResponseDto mapToResponseDto(StaffUser user) {
        StaffResponseDto dto = new StaffResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        return dto;
    }
}
