package com.ojt_22.mmspg.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.ApiCallLog;

@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, UUID> {

    
    Page<ApiCallLog> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId, Pageable pageable);

    
    Page<ApiCallLog> findByRequestId(String requestId, Pageable pageable);
}