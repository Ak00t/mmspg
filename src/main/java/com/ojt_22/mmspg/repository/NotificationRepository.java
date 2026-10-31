package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	List<Notification> findTop10ByStaffIdAndIsReadFalseOrderByCreatedAtDesc(UUID staffId);

	List<Notification> findTop20ByStaffIdOrderByCreatedAtDesc(UUID staffId);

	long countByStaffIdAndIsReadFalse(UUID staffId);
}
