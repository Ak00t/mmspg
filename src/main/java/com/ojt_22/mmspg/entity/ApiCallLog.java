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
@Table(name = "api_call_logs")
@Getter @Setter @NoArgsConstructor
public class ApiCallLog extends UuidV7Entity {
    @Id
    @Column(name = "log_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_id", columnDefinition = "BINARY(16)") private UUID merchantId;
    @Column(name = "credential_id", columnDefinition = "BINARY(16)") private UUID credentialId;
    @Column(nullable = false, length = 255) private String endpoint;
    @Column(name = "http_method", nullable = false, length = 10) private String httpMethod;
    @Column(name = "request_id", length = 100) private String requestId;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "request_body", columnDefinition = "json") private String requestBody;
    @Column(name = "response_status") private Integer responseStatus;
    @Column(name = "response_time_ms") private Long responseTimeMs;
    @Lob @Column(name = "error_message", columnDefinition = "TEXT") private String errorMessage;
    @Lob @Column(name = "query_params", columnDefinition = "TEXT") private String queryParams;
    @Column(name = "user_agent", length = 225) private String userAgent;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
