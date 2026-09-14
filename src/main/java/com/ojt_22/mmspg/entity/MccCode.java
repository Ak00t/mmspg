package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ojt_22.mmspg.enums.MccCodeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mcc_codes")
@Getter
@Setter
@NoArgsConstructor
public class MccCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mcc_id")
	private Integer id;

	@Column(name = "mcc_code", nullable = false, length = 10, unique = true)
	private String mccCode;

	@Column(name = "mcc_name", nullable = false, length = 150)
	private String mccName;

	@Column(length = 255)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')")
	private MccCodeStatus status;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", updatable = false)
	private StaffUser createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private StaffUser updatedBy;
}
