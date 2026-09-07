package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
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
@Table(name = "merchant_ledgers")
@Getter
@Setter
@NoArgsConstructor
public class MerchantLedgerEntry {

	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "ledger_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id", nullable = false)
	private Merchant merchant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_id")
	private PaymentTransaction transaction;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_id")
	private Settlement settlement;

	@Column(name = "entry_type", nullable = false, columnDefinition = "enum('DEBIT','CREDIT')")
	private String entryType;

	@Column(name = "balance_type", nullable = false, columnDefinition = "enum('CLEARED','PENDING','HELD')")
	private String balanceType = "PENDING";

	@Column(nullable = false, precision = 18, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, length = 255)
	private String description;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

}
