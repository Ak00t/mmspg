package com.ojt_22.mmspg.service;

import java.util.List;

import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.dto.MerchantResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import org.springframework.web.multipart.MultipartFile;

public interface MerchantService {
    Merchant registerMerchant(MerchantRegistrationRequest request);
    org.springframework.data.domain.Page<com.ojt_22.mmspg.dto.MerchantPendingDto> getPendingMerchants(int page, int size);
    void approveMerchant(java.util.UUID merchantId);
    void rejectMerchant(java.util.UUID merchantId, String reason);
    void updateMerchant(java.util.UUID merchantId, String businessName, String merchantCode);
    void updateMerchantStatus(java.util.UUID merchantId, String status, String reason);
    List<MerchantResponseDto> getAllMerchants();
    
    MerchantProfileResponseDto getMerchantProfile(java.util.UUID merchantId);
    String uploadMerchantProfilePicture(java.util.UUID merchantId, MultipartFile file);
}
