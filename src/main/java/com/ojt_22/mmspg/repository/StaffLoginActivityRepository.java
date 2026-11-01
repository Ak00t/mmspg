package com.ojt_22.mmspg.repository;

import com.ojt_22.mmspg.entity.StaffLoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffLoginActivityRepository extends JpaRepository<StaffLoginActivity, UUID> {
    // Returns the single most recent session for display on the profile page
    Optional<StaffLoginActivity> findFirstByStaffIdOrderByLoginTimeDesc(UUID staffId);
    // Finds all sessions with a given status (used to close old ACTIVE sessions on new login)
    List<StaffLoginActivity> findByStaffIdAndStatus(UUID staffId, String status);
    // Finds the latest ACTIVE session to mark as LOGGED_OUT on logout
    Optional<StaffLoginActivity> findFirstByStaffIdAndStatusOrderByLoginTimeDesc(UUID staffId, String status);
}
