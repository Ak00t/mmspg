package com.ojt_22.mmspg.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.StaffUser;

@Repository
public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {
    Optional<StaffUser> findByUsername(String username);
    Optional<StaffUser> findByEmail(String email);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
