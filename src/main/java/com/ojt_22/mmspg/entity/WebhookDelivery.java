package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "webhook_deliveries") @Getter @Setter @NoArgsConstructor
public class WebhookDelivery extends UuidV7Entity {
    @Id @Column(name = "delivery_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "webhook_id", nullable = false, columnDefinition = "BINARY(16)") private UUID webhookId;
    @Column(name = "transaction_id", nullable = false, columnDefinition = "BINARY(16)") private UUID transactionId;
    @Column(name = "event_type", nullable = false, length = 100) private String eventType;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json") private String payload;
    @Column(name = "response_status") private Integer responseStatus;
    @Lob @Column(name = "response_body", columnDefinition = "TEXT") private String responseBody;
    @Column(name = "attempt_count", nullable = false) private Integer attemptCount;
    @Column(nullable = false, columnDefinition = "enum('PENDING','SENT','DELIVERED','FAILED')") private String status;
    @Column(name = "sent_at") private LocalDateTime sentAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    @Lob @Column(name = "error_message", columnDefinition = "TEXT") private String errorMessage;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
    @Column(name = "next_retry_at", nullable = false) private LocalDateTime nextRetryAt;
}
