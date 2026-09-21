package com.ojt_22.mmspg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.WebhookDelivery;
import com.ojt_22.mmspg.enums.WebhookDeliveryStatus;


@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {
	
	List<WebhookDelivery> findByTransactionIdOrderByCreatedAtDesc(UUID transactionId);
	
	@Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.merchant.id = :merchantId ORDER BY wd.createdAt DESC")
    Page<WebhookDelivery> findByMerchantId(@Param("merchantId") UUID merchantId, Pageable pageable);
	
	List<WebhookDelivery> findByStatusAndNextRetryAtBefore(WebhookDeliveryStatus status, LocalDateTime now);
	
}