package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "api_call_logs")
@Getter
@Setter
@NoArgsConstructor
public class ApiCallLog {
	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "log_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "credential_id")
	private ApiCredential credential;

	@Column(nullable = false, length = 255)
	private String endpoint;

	@Column(name = "http_method", nullable = false, length = 10)
	private String httpMethod;

	@Column(name = "request_id", length = 100)
	private String requestId;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "request_body", columnDefinition = "json")
	private String requestBody;

	@Column(name = "response_status")
	private Integer responseStatus;

	@Column(name = "response_time_ms")
	private Long responseTimeMs;

	@Lob
	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Lob
	@Column(name = "query_params", columnDefinition = "TEXT")
	private String queryParams;

	@Column(name = "user_agent", length = 225)
	private String userAgent;

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
