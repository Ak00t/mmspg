package com.ojt_22.mmspg.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    
    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.createdAt >= :startDate AND pt.createdAt < :endDate AND pt.status = 'COMPLETED'")
    Optional<BigDecimal> sumAmountByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

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

    // 1. Transaction Reference ဖြင့် ရှာရန် (Status API အတွက်)
    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);

    // 2. Payment Token ဖြင့် ရှာရန် (Authorize API အတွက်)
    Optional<PaymentTransaction> findByPaymentToken(String paymentToken);

   
    List<PaymentTransaction> findByMerchantId(UUID merchantId);
    
    // 4. Merchant တစ်ခုတည်းအောက်တွင် Order ID ထပ်မထပ် စစ်ရန်
    boolean existsByMerchantIdAndOrderId(UUID merchantId, String orderId);
    
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

}
