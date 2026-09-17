package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.ojt_22.mmspg.enums.TerminalStatus;
import com.ojt_22.mmspg.enums.TerminalType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "terminals", uniqueConstraints = @UniqueConstraint(columnNames = "terminal_code"))
@Getter
@Setter
@NoArgsConstructor
public class Terminal {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(name = "terminal_id", columnDefinition = "BINARY(16)")
	private UUID id;


	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	private MerchantBranch branch;

	@Column(name = "terminal_code", nullable = false, length = 50)
	private String terminalCode;

	@Column(name = "terminal_name", length = 100)
	private String terminalName;

	@Column(name = "terminal_type", nullable = false, columnDefinition = "enum('PHYSICAL_POS','VIRTUAL_API')")
	private TerminalType terminalType;

	@Column(nullable = false, columnDefinition = "enum('ONLINE','OFFLINE','SUSPENDED')")
	private TerminalStatus status = TerminalStatus.ONLINE;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false, updatable = false)
	private StaffUser createdBy;
}
