package com.ojt_22.mmspg.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ojt_22.mmspg.entity.Settlement;
import com.ojt_22.mmspg.enums.SettlementStatus;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

	boolean existsByTransactionId(UUID transactionId);

	@Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM Settlement s " + "WHERE s.merchant.id = :merchantId "
			+ "AND s.status = 'PENDING'")
	BigDecimal findAvailableSettlementBalance(UUID merchantId);

	Long countByMerchantIdAndStatus(UUID merchantId, SettlementStatus status);

}
