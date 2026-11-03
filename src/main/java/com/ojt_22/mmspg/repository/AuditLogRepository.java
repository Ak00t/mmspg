package com.ojt_22.mmspg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ojt_22.mmspg.entity.AuditLog;
import com.ojt_22.mmspg.enums.AuditStatus;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId);

	List<AuditLog> findByMenuNameAndAction(String menuName, String action);

	Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

	@Query("SELECT a FROM AuditLog a WHERE "
			+ "(:menuName IS NULL OR :menuName = '' OR LOWER(a.menuName) LIKE LOWER(CONCAT('%', :menuName, '%'))) AND "
			+ "(:action IS NULL OR :action = '' OR LOWER(a.action) LIKE LOWER(CONCAT('%', :action, '%'))) AND "
			+ "(:status IS NULL OR a.status = :status) AND "
			+ "(:startDate IS NULL OR a.createdAt >= :startDate) AND "
			+ "(:endDate IS NULL OR a.createdAt <= :endDate) "
			+ "ORDER BY a.createdAt DESC")
	Page<AuditLog> findWithFilters(
			@Param("menuName") String menuName,
			@Param("action") String action,
			@Param("status") AuditStatus status,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			Pageable pageable);

	List<AuditLog> findTop10ByOrderByCreatedAtDesc();
}

