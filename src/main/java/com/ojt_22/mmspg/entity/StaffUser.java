package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "staff_users") @Getter @Setter @NoArgsConstructor
public class StaffUser extends UuidV7Entity {
    @Id @Column(name = "staff_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(nullable = false, length = 100, unique = true) private String username;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    @Column(length = 150, unique = true) private String email;
    @Column(nullable = false, columnDefinition = "enum('ADMIN','STAFF')") private String role;
    @Column(nullable = false, columnDefinition = "enum('ACTIVE','DISABLED')") private String status;
    @Column(name = "last_login_at") private LocalDateTime lastLoginAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
