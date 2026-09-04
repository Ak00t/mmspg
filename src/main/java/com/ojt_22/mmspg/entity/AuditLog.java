package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor
public class AuditLog extends UuidV7Entity {
    @Id @Column(name = "audit_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "staff_id", nullable = false, columnDefinition = "BINARY(16)") private UUID staffId;
    @Column(nullable = false, length = 100) private String action;
    @Column(name = "entity_type", nullable = false, length = 100) private String entityType;
    @Column(name = "entity_id", nullable = false, columnDefinition = "BINARY(16)") private UUID entityId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "old_value", columnDefinition = "json") private String oldValue;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "new_value", columnDefinition = "json") private String newValue;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
