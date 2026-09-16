package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ojt_22.mmspg.entity.MerchantBranch;

@Repository
public interface BranchRepository extends JpaRepository<MerchantBranch, UUID> {
    List<MerchantBranch> findByMerchantId(UUID merchantId);
    boolean existsByBranchCodeAndMerchantId(String branchCode, UUID merchantId);
}
