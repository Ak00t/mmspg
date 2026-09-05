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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
public class Merchant {

	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "merchant_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(name = "merchant_code", nullable = false, length = 50, unique = true)
	private String merchantCode;

	@Column(name = "business_name", nullable = false, length = 200)
	private String businessName;

	@Column(name = "business_registration_no", length = 100)
	private String businessRegistrationNo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mcc_id", nullable = false)
	private MccCode mccCode;

	@Column(name = "settlement_account_no", nullable = false, length = 100)
	private String settlementAccountNo;

	@Column(name = "contact_name", length = 150)
	private String contactName;

	@Column(length = 150)
	private String email;

	@Column(length = 30)
	private String phone;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String address;

	@Column(nullable = false, columnDefinition = "enum('PENDING','ACTIVE','REJECTED','SUSPENDED','CLOSED')")
	private String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approved_by")
	private StaffUser approvedBy;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

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

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;
}
