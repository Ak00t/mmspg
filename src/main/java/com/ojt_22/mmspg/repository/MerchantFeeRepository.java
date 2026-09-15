package com.ojt_22.mmspg.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.MerchantFee;

@Repository
public interface MerchantFeeRepository extends JpaRepository<MerchantFee, UUID> {
    Optional<MerchantFee> findByMerchantId(UUID merchantId);
}