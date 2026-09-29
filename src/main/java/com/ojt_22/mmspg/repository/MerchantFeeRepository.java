package com.ojt_22.mmspg.repository;


import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.MerchantFee;

@Repository
public interface MerchantFeeRepository extends JpaRepository<MerchantFee, UUID> {
    
    @Query("SELECT mf FROM MerchantFee mf JOIN FETCH mf.merchant m ORDER BY mf.createdAt DESC")
    List<MerchantFee> findAllWithMerchant();

    Optional<MerchantFee> findByMerchantId(UUID merchantId);
    
    @Query("""
    	    SELECT mf
    	    FROM MerchantFee mf
    	    WHERE mf.merchant.id = :merchantId
    	      AND mf.status = 'ACTIVE'
    	      AND mf.effectiveFrom <= :now
    	      AND (mf.effectiveTo IS NULL OR mf.effectiveTo >= :now)
    	    ORDER BY mf.effectiveFrom DESC
    	    """)
    	List<MerchantFee> findCurrentFees(
    	        UUID merchantId,
    	        LocalDateTime now);
}
