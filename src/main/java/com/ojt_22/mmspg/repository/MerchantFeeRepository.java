package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.MerchantFee;

@Repository
public interface MerchantFeeRepository extends JpaRepository<MerchantFee, UUID> {
    
    @Query("SELECT mf FROM MerchantFee mf JOIN FETCH mf.merchant m ORDER BY mf.createdAt DESC")
    List<MerchantFee> findAllWithMerchant();
}
