package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs", indexes = { @Index(name = "idx_audit_actor", columnList = "actor_id, created_at"),
		@Index(name = "idx_audit_menu_action", columnList = "menu_name, action"),
		@Index(name = "idx_audit_status", columnList = "status") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "audit_id", nullable = false, updatable = false)
	private Long auditId;

	@Column(name = "actor_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
	private UUID actorId;

	@Column(name = "actor_type", length = 50, nullable = false, updatable = false)
	private String actorType;

	@Column(name = "role_name", length = 50, nullable = false, updatable = false)
	private String roleName;

	@Column(name = "permission_used", length = 100, updatable = false)
	private String permissionUsed;

	@Column(name = "menu_name", length = 100, nullable = false, updatable = false)
	private String menuName;

	@Column(name = "action", length = 50, nullable = false, updatable = false)
	private String action;

	@Column(name = "description", length = 255, updatable = false)
	private String description;

	@Column(name = "target_type", length = 50, updatable = false)
	private String targetType;

	@Column(name = "target_id", length = 100, updatable = false)
	private String targetId; // Primary key of target object

	@Column(name = "ip_address", length = 45, updatable = false)
	private String ipAddress; // Client IPv4 / IPv6

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false, updatable = false)
	private AuditStatus status = AuditStatus.SUCCESS;// SUCCESS or FAILURE

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", length = 20, nullable = false, updatable = false)
	private SourceType sourceType = SourceType.BACKEND_API; // FRONTEND_UI or BACKEND_API

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public enum AuditStatus {
		SUCCESS, FAILURE
	}

	public enum SourceType {
		FRONTEND_UI, BACKEND_API
	}
}