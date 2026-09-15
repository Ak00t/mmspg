package com.ojt_22.mmspg.service;

import com.ojt_22.mmspg.dto.MerchantRegistrationRequest;
import com.ojt_22.mmspg.entity.Merchant;

public interface MerchantService {
    Merchant registerMerchant(MerchantRegistrationRequest request);
    org.springframework.data.domain.Page<com.ojt_22.mmspg.dto.MerchantPendingDto> getPendingMerchants(int page, int size);
    void approveMerchant(java.util.UUID merchantId);
    void rejectMerchant(java.util.UUID merchantId, String reason);
}
