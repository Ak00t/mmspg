package com.ojt_22.mmspg.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.Merchant;
// 🔴 MerchantStatus ကို Import လုပ်ရန်
import com.ojt_22.mmspg.enums.MerchantStatus;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMerchantCode(String merchantCode);
    
    // 🔴 String အစား MerchantStatus ဟု ပြောင်းလဲပါ
    long countByStatus(MerchantStatus status);
    
    java.util.List<Merchant> findTop10ByOrderByCreatedAtDesc();
    
    // 🔴 ဤနေရာတွင်လည်း String အစား MerchantStatus ဟု ပြောင်းလဲပါ
    org.springframework.data.domain.Page<Merchant> findByStatus(MerchantStatus status, org.springframework.data.domain.Pageable pageable);
}