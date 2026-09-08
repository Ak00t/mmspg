package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "api_credentials")
@Getter
@Setter
@NoArgsConstructor
public class ApiCredential {
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "credential_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id", nullable = false)
	private Merchant merchant;

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

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private StaffUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private StaffUser updatedBy;
}
