package com.ojt_22.mmspg.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ojt_22.mmspg.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

}
