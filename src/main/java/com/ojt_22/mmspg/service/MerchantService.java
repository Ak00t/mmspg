package com.ojt_22.mmspg.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ojt_22.mmspg.dto.MerchantPendingDto;
import com.ojt_22.mmspg.dto.MerchantProfileResponseDto;
import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.dto.MerchantResponseDto;
import com.ojt_22.mmspg.entity.Merchant;

public interface MerchantService {
    Merchant registerMerchant(MerchantRegistrationRequest request);
    Page<MerchantPendingDto> getPendingMerchants(int page, int size);
    void approveMerchant(UUID merchantId);
    void rejectMerchant(UUID merchantId, String reason);
    void reactivateMerchant(UUID merchantId);
    void updateMerchant(UUID merchantId, String businessName, String merchantCode);
    void updateMerchantStatus(UUID merchantId, String status, String reason);
    List<MerchantResponseDto> getAllMerchants();
    MerchantProfileResponseDto getMerchantProfile(UUID merchantId);
    String uploadMerchantProfilePicture(UUID merchantId, MultipartFile file);
}
