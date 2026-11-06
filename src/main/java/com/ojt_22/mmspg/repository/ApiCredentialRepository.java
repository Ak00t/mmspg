package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.ApiCredential;

import com.ojt_22.mmspg.enums.ApiCredentialStatus;

@Repository
public interface ApiCredentialRepository extends JpaRepository<ApiCredential, UUID> {

    Optional<ApiCredential> findByClientIdAndStatus(String clientId, ApiCredentialStatus status);

    List<ApiCredential> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    boolean existsByClientId(String clientId);
}