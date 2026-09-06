package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "webhook_configs")
@Getter
@Setter
@NoArgsConstructor
public class WebhookConfig {

	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "webhook_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id", nullable = false)
	private Merchant merchant;

	@Column(name = "callback_url", nullable = false, length = 500)
	private String callbackUrl;

	@Column(name = "secret_key_hash", length = 255)
	private String secretKeyHash;

	@Column(name = "event_payment_completed", nullable = false)
	private Boolean eventPaymentCompleted;

	@Column(name = "event_payment_failed", nullable = false)
	private Boolean eventPaymentFailed;

	@Column(name = "max_retry", nullable = false)
	private Integer maxRetry;

	@Column(length = 255)
	private String description;

	@Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')")
	private String status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private StaffUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private StaffUser updatedBy;
}
