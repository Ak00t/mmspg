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
@Table(name = "staff_users")
@Getter
@Setter
@NoArgsConstructor
public class StaffUser {

	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "staff_id", columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(nullable = false, length = 100, unique = true)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "full_name", nullable = false, length = 150)
	private String fullName;

	@Column(length = 150, unique = true)
	private String email;

	@Column(nullable = false, columnDefinition = "enum('ADMIN','STAFF')")
	private String role;

	@Column(nullable = false, columnDefinition = "enum('ACTIVE','DISABLED')")
	private String status;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

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
