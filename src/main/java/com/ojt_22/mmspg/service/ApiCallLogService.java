package com.ojt_22.mmspg.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.ApiCallLogDto;
import com.ojt_22.mmspg.entity.ApiCallLog;
import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.ApiCallLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiCallLogService {

    private final ApiCallLogRepository apiCallLogRepository;

   
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLog(
            Merchant merchant,
            ApiCredential credential,
            String endpoint,
            String httpMethod,
            String requestId,
            String ipAddress,
            String requestBody,
            Integer responseStatus,
            Long responseTimeMs,
            String errorMessage,
            String queryParams,
            String userAgent
    ) {
        ApiCallLog log = new ApiCallLog();
        log.setMerchant(merchant);
        log.setCredential(credential);
        log.setEndpoint(endpoint);
        log.setHttpMethod(httpMethod);
        log.setRequestId(requestId);
        log.setIpAddress(ipAddress);
        log.setRequestBody(requestBody);
        log.setResponseStatus(responseStatus);
        log.setResponseTimeMs(responseTimeMs);
        log.setErrorMessage(errorMessage);
        log.setQueryParams(queryParams);
        log.setUserAgent(userAgent);

        apiCallLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ApiCallLogDto> getLogsByMerchant(UUID merchantId, Pageable pageable) {
        return apiCallLogRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable)
                .map(log -> ApiCallLogDto.builder()
                        .logId(log.getId())
                        .endpoint(log.getEndpoint())
                        .httpMethod(log.getHttpMethod())
                        .requestId(log.getRequestId())
                        .ipAddress(log.getIpAddress())
                        .responseStatus(log.getResponseStatus())
                        .responseTimeMs(log.getResponseTimeMs())
                        .errorMessage(log.getErrorMessage())
                        .createdAt(log.getCreatedAt())
                        .build());
    }
}