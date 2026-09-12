package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.StaffUserRegisterRequest;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.service.StaffUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffUserServiceImpl implements StaffUserService {

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StaffUser registerStaff(StaffUserRegisterRequest request) {
        
        if (staffUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (staffUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        StaffUser staffUser = new StaffUser();
        staffUser.setUsername(request.getUsername());
        staffUser.setEmail(request.getEmail());
        staffUser.setFullName(request.getFullName());
        staffUser.setRole(request.getRole());
        staffUser.setStatus("ACTIVE");
        
        // Hash the password
        staffUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // Setting updated_at is required due to schema constraint
        staffUser.setUpdatedAt(LocalDateTime.now());

        return staffUserRepository.save(staffUser);
    }
    
    @Override
    public List<StaffUser> getAllStaff() {
        return staffUserRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.ojt_22.mmspg.dto.StaffResponseDto> getAllStaffUsersDto() {
        return staffUserRepository.findAll().stream().map(this::mapToResponseDto).toList();
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
        
        String role = request.getRole().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("AUDITOR") && !role.equals("SUPPORT")) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, AUDITOR, or SUPPORT.");
        }
        staffUser.setRole(role);
        staffUser.setStatus("ACTIVE");
        
        staffUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        staffUser.setUpdatedAt(LocalDateTime.now());

        StaffUser saved = staffUserRepository.save(staffUser);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void toggleStaffStatus(java.util.UUID staffId) {
        StaffUser staffUser = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found with ID: " + staffId));

        if ("ACTIVE".equals(staffUser.getStatus())) {
            staffUser.setStatus("DISABLED");
        } else {
            staffUser.setStatus("ACTIVE");
        }
        
        staffUser.setUpdatedAt(LocalDateTime.now());
        staffUserRepository.save(staffUser);
    }

    private com.ojt_22.mmspg.dto.StaffResponseDto mapToResponseDto(StaffUser user) {
        com.ojt_22.mmspg.dto.StaffResponseDto dto = new com.ojt_22.mmspg.dto.StaffResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
