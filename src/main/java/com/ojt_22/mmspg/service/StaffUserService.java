package com.ojt_22.mmspg.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.StaffRequestDto;
import com.ojt_22.mmspg.dto.StaffResponseDto;
import com.ojt_22.mmspg.dto.StaffUserRegisterRequest;
import com.ojt_22.mmspg.dto.UserProfileResponseDto;
import com.ojt_22.mmspg.entity.StaffUser;

public interface StaffUserService {
    StaffUser registerStaff(StaffUserRegisterRequest request);
    List<StaffUser> getAllStaff();
    List<StaffResponseDto> getAllStaffUsersDto();
    StaffResponseDto createStaffUser(StaffRequestDto request);
    void toggleStaffStatus(UUID staffId);
    void updateStaffStatus(UUID staffId, String status);
    void reactivateStaff(UUID staffId);
    void updateStaffUser(UUID id, String fullName, String email, String role);
    void deleteStaffUser(UUID id);
    UserProfileResponseDto getStaffProfile(UUID staffId);
    void changeStaffPassword(UUID staffId, ChangePasswordRequestDto request);
    String uploadProfilePicture(UUID id, MultipartFile file);
}
