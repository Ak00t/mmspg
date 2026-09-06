package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "merchant_fees")
@Getter
@Setter
@NoArgsConstructor
public class MerchantFee {
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "fee_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id", nullable = false)
	private Merchant merchant;

	@Column(name = "fee_type", nullable = false, columnDefinition = "enum('PERCENTAGE','FLAT','MIXED')")
	private String feeType;

	@Column(name = "percentage_rate", precision = 5, scale = 2)
	private BigDecimal percentageRate;

	@Column(name = "flat_fee", precision = 18, scale = 3)
	private BigDecimal flatFee;

	@Column(name = "effective_from", nullable = false)
	private LocalDateTime effectiveFrom;

	@Column(name = "effective_to")
	private LocalDateTime effectiveTo;

	@Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')")
	private String status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private StaffUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private StaffUser updatedBy;
}
