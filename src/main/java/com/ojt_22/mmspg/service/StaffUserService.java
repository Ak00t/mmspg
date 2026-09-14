package com.ojt_22.mmspg.service;

import java.util.List;

import com.ojt_22.mmspg.dto.StaffUserRegisterRequest;
import com.ojt_22.mmspg.entity.StaffUser;

public interface StaffUserService {
    StaffUser registerStaff(StaffUserRegisterRequest request);
    List<StaffUser> getAllStaff();
    
    java.util.List<com.ojt_22.mmspg.dto.StaffResponseDto> getAllStaffUsersDto();
    com.ojt_22.mmspg.dto.StaffResponseDto createStaffUser(com.ojt_22.mmspg.dto.StaffRequestDto request);
    void toggleStaffStatus(java.util.UUID staffId);
}
