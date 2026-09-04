package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "merchants") @Getter @Setter @NoArgsConstructor
public class Merchant extends UuidV7Entity {
    @Id @Column(name = "merchant_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_code", nullable = false, length = 50, unique = true) private String merchantCode;
    @Column(name = "business_name", nullable = false, length = 200) private String businessName;
    @Column(name = "business_registration_no", length = 100) private String businessRegistrationNo;
    @Column(name = "mcc_id", nullable = false) private Integer mccId;
    @Column(name = "settlement_account_no", nullable = false, length = 100) private String settlementAccountNo;
    @Column(name = "contact_name", length = 150) private String contactName;
    @Column(length = 150) private String email;
    @Column(length = 30) private String phone;
    @Lob @Column(columnDefinition = "TEXT") private String address;
    @Column(nullable = false, columnDefinition = "enum('PENDING','ACTIVE','REJECTED','SUSPENDED','CLOSED')") private String status;
    @Column(name = "approved_by", columnDefinition = "BINARY(16)") private UUID approvedBy;
    @Column(name = "approved_at") private LocalDateTime approvedAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
}
