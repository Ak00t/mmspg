package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Immutable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "audit_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "staff_id", nullable = false)
	private StaffUser staff;

	@Column(nullable = false, length = 100)
	private String action;

	@Column(name = "entity_type", nullable = false, length = 100)
	private String entityType;

	@Column(name = "entity_id", nullable = false, columnDefinition = "BINARY(16)")
	private UUID entityId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "old_value", columnDefinition = "json")
	private String oldValue;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "new_value", columnDefinition = "json")
	private String newValue;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

}
