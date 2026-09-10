package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
public class PaymentTransaction {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "transaction_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id", nullable = false)
	private Merchant merchant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private MerchantBranch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "terminal_id")
	private Terminal terminal;

	@Column(name = "order_id", nullable = false, length = 100)
	private String orderId;

	@Column(name = "transaction_reference", nullable = false, length = 100, unique = true)
	private String transactionReference;

	@Column(name = "core_transaction_reference", length = 100)
	private String coreTransactionReference;

	@Column(name = "payment_token", nullable = false, length = 255)
	private String paymentToken;

	@Column(nullable = false, precision = 18, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, length = 3, columnDefinition = "char(3)")
	private String currency = "MMK";

	@Column(name = "fee_amount", nullable = false, precision = 18, scale = 4)
	private BigDecimal feeAmount;

	@Column(name = "net_amount", nullable = false, precision = 18, scale = 4)
	private BigDecimal netAmount;

	@Column(name = "return_url", length = 500)
	private String returnUrl;

	@Column(nullable = false, columnDefinition = "enum('INITIATED','PENDING_AUTHORIZATION','COMPLETED','FAILED')")
	private String status;

	@Column(name = "failure_reason", length = 500)
	private String failureReason;

	@Column(name = "initiated_at", nullable = false)
	private LocalDateTime initiatedAt;

	@Column(name = "authorized_at")
	private LocalDateTime authorizedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "failed_at")
	private LocalDateTime failedAt;

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
