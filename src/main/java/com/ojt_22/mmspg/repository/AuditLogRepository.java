package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ojt_22.mmspg.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);

	List<AuditLog> findByMenuNameAndAction(String menuName, String action);

}
