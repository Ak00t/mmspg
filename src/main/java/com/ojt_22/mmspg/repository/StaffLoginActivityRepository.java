package com.ojt_22.mmspg.repository;

import com.ojt_22.mmspg.entity.StaffLoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface StaffLoginActivityRepository extends JpaRepository<StaffLoginActivity, UUID> {
    List<StaffLoginActivity> findTop4ByStaffIdOrderByLoginTimeDesc(UUID staffId);
    java.util.List<StaffLoginActivity> findByStaffIdAndDeviceAndBrowser(UUID staffId, String device, String browser);
}