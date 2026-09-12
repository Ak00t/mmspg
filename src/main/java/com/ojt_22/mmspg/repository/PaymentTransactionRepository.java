package com.ojt_22.mmspg.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    
    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.createdAt >= :startDate AND pt.createdAt < :endDate AND pt.status = 'COMPLETED'")
    Optional<BigDecimal> sumAmountByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
