package com.ojt_22.mmspg.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.Merchant;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMerchantCode(String merchantCode);
    long countByStatus(String status);
    java.util.List<Merchant> findTop10ByOrderByCreatedAtDesc();
    org.springframework.data.domain.Page<Merchant> findByStatus(String status, org.springframework.data.domain.Pageable pageable);
}
