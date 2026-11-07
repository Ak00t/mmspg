package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.MerchantBranch;

@Repository
public interface BranchRepository extends JpaRepository<MerchantBranch, UUID> {
    List<MerchantBranch> findByMerchantId(UUID merchantId);
    List<MerchantBranch> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
    boolean existsByBranchCodeAndMerchantId(String branchCode, UUID merchantId);

    @Query("SELECT b.branchCode FROM MerchantBranch b WHERE b.merchant.id = :merchantId AND b.branchCode LIKE :prefix")
    List<String> findBranchCodesByMerchantAndPrefix(@Param("merchantId") UUID merchantId, @Param("prefix") String prefix);
}
