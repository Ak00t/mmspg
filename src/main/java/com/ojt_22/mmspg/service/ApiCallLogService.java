package com.ojt_22.mmspg.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ojt_22.mmspg.dto.ApiCallLogDto;
import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;

public interface ApiCallLogService {

	public void recordLog(Merchant merchant, ApiCredential credential, String endpoint, String httpMethod,
			String requestId, String ipAddress, String requestBody, Integer responseStatus, Long responseTimeMs,
			String errorMessage, String queryParams, String userAgent);

	public Page<ApiCallLogDto> getLogsByMerchant(UUID merchantId, Pageable pageable);

}
