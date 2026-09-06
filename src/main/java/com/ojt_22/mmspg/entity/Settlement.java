package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "settlements")
@Getter
@Setter
@NoArgsConstructor
public class Settlement {

	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "settlement_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id", nullable = false)
	private Merchant merchant;

	@Column(name = "settlement_reference", nullable = false, length = 100, unique = true)
	private String settlementReference;

	@Column(name = "settlement_date", nullable = false)
	private LocalDate settlementDate;

	@Column(name = "gross_amount", nullable = false, precision = 18, scale = 4)
	private BigDecimal grossAmount;

	@Column(name = "fee_amount", nullable = false, precision = 18, scale = 4)
	private BigDecimal feeAmount;

	@Column(name = "refund_amount", nullable = false, precision = 18, scale = 4)
	private BigDecimal refundAmount;

	@Column(name = "net_amount", nullable = false, precision = 18, scale = 4)
	private BigDecimal netAmount;

	@Column(name = "bank_account_no", nullable = false, length = 100)
	private String bankAccountNo;

	@Column(nullable = false, columnDefinition = "enum('PENDING','PROCESSING','COMPLETED','FAILED')")
	private String status;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

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
