package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode; // 🔴 ဤအရာကို Import လုပ်ပါ
import org.hibernate.annotations.UuidGenerator;

import java.sql.Types; // 🔴 ဤအရာကို Import လုပ်ပါ

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@JdbcTypeCode(Types.VARCHAR) // 🔴 ထည့်ပေးပါ
	@Column(name = "id", columnDefinition = "VARCHAR(36)") // 🔴 BINARY(16) အစား VARCHAR(36) ပြောင်းပါ
	private UUID id;

	@JdbcTypeCode(Types.VARCHAR) // 🔴 ထည့်ပေးပါ
	@Column(name = "staff_id", columnDefinition = "VARCHAR(36)", nullable = false) // 🔴 BINARY(16) အစား VARCHAR(36) ပြောင်းပါ
	private UUID staffId;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "type", nullable = false, length = 50)
	private String type;

	@Column(name = "is_read", nullable = false)
	@Builder.Default
	private Boolean isRead = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (this.isRead == null) {
			this.isRead = false;
		}
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}