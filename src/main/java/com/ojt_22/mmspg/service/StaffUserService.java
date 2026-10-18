package com.ojt_22.mmspg.service;

import java.util.List;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.UserProfileResponseDto;
import com.ojt_22.mmspg.dto.StaffUserRegisterRequest;
import com.ojt_22.mmspg.entity.StaffUser;

public interface StaffUserService {
    StaffUser registerStaff(StaffUserRegisterRequest request);
    List<StaffUser> getAllStaff();
    
    java.util.List<com.ojt_22.mmspg.dto.StaffResponseDto> getAllStaffUsersDto();
    com.ojt_22.mmspg.dto.StaffResponseDto createStaffUser(com.ojt_22.mmspg.dto.StaffRequestDto request);
    void toggleStaffStatus(java.util.UUID staffId);
    void updateStaffUser(java.util.UUID id, String fullName, String email, String role);
    void deleteStaffUser(java.util.UUID id);
    
    UserProfileResponseDto getStaffProfile(java.util.UUID staffId);
    void changeStaffPassword(java.util.UUID staffId, ChangePasswordRequestDto request);
    String uploadProfilePicture(java.util.UUID id, org.springframework.web.multipart.MultipartFile file);
}
