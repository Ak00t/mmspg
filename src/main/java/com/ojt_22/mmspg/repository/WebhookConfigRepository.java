package com.ojt_22.mmspg.repository;

import java.util.Optional;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.WebhookConfig;

@Repository

public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, UUID> {
	
	Optional<WebhookConfig> findByMerchantIdAndStatus(UUID merchantId, String status);

	boolean existsByMerchantIdAndStatus(UUID merchantId, String status);
}
