package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "terminals", uniqueConstraints = @UniqueConstraint(columnNames = "terminal_code")) @Getter @Setter @NoArgsConstructor
public class Terminal extends UuidV7Entity {
    @Id @Column(name = "terminal_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "branch_id", nullable = false, columnDefinition = "BINARY(16)") private UUID branchId;
    @Column(name = "terminal_code", nullable = false, length = 50) private String terminalCode;
    @Column(name = "terminal_name", length = 100) private String terminalName;
    @Column(name = "terminal_type", length = 45) private String terminalType;
    @Column(nullable = false, columnDefinition = "enum('ACTIVE','SUSPENDED','DISABLED')") private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
}
