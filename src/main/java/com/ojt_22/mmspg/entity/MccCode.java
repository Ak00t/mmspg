package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mcc_codes")
@Getter @Setter @NoArgsConstructor
public class MccCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "mcc_id") private Integer id;
    @Column(name = "mcc_code", nullable = false, length = 10, unique = true) private String mccCode;
    @Column(name = "mcc_name", nullable = false, length = 150) private String mccName;
    @Column(length = 255) private String description;
    @Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')") private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
