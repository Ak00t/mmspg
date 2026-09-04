package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "merchant_branches", uniqueConstraints = @UniqueConstraint(columnNames = {"merchant_id", "branch_code"}))
@Getter @Setter @NoArgsConstructor
public class MerchantBranch extends UuidV7Entity {
    @Id @Column(name = "branch_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)") private UUID merchantId;
    @Column(name = "branch_code", nullable = false, length = 50) private String branchCode;
    @Column(name = "branch_name", nullable = false, length = 150) private String branchName;
    @Lob @Column(columnDefinition = "TEXT") private String address;
    @Column(length = 30) private String phone;
    @Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')") private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
