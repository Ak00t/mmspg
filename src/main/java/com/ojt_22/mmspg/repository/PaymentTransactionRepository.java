package com.ojt_22.mmspg.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ojt_22.mmspg.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t " + "WHERE t.merchant.id = :merchantId "
			+ "AND t.status = 'COMPLETED' " + "AND t.createdAt >= :startOfDay AND t.createdAt <= :endOfDay")
	BigDecimal sumGrossSalesByDateRange(@Param("merchantId") UUID merchantId,
			@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("SELECT COUNT(t) FROM PaymentTransaction t " + "WHERE t.merchant.id = :merchantId "
			+ "AND t.createdAt >= :startOfDay AND t.createdAt <= :endOfDay")
	Long countTransactionsByDateRange(@Param("merchantId") UUID merchantId,
			@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("SELECT t.status, COUNT(t) FROM PaymentTransaction t " + "WHERE t.merchant.id = :merchantId "
			+ "AND t.createdAt >= :startOfDay AND t.createdAt <= :endOfDay " + "GROUP BY t.status")
	List<Object[]> countGroupedByStatus(@Param("merchantId") UUID merchantId,
			@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

}
