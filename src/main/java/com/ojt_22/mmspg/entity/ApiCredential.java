package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "api_credentials")
@Getter
@Setter
@NoArgsConstructor
public class ApiCredential extends UuidV7Entity {
	@Id
	@Column(name = "credential_id", columnDefinition = "BINARY(16)")
	private UUID id;
	@Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)")
	private UUID merchantId;
	@Column(name = "client_id", nullable = false, length = 100, unique = true)
	private String clientId;
	@Column(name = "client_secret_hash", nullable = false, length = 255)
	private String clientSecretHash;
	@Column(name = "ip_address", nullable = false, length = 45)
	private String ipAddress;
	@Column(nullable = false, columnDefinition = "enum('SANDBOX','PRODUCTION')")
	private String environment;
	@Column(nullable = false, columnDefinition = "enum('ACTIVE','REVOKED','EXPIRED')")
	private String status;
	@Column(name = "key_name", nullable = false, length = 100)
	private String keyName;
	@Column(name = "expires_at")
	private LocalDateTime expiresAt;
	@Column(name = "rate_limit")
	private Integer rateLimit;
	@Column(name = "last_used_at")
	private LocalDateTime lastUsedAt;
	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	@Column(name = "created_by", columnDefinition = "BINARY(16)")
	private UUID createdBy;
	@Column(name = "updated_by", columnDefinition = "BINARY(16)")
	private UUID updatedBy;
}
