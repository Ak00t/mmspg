package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "webhook_configs") @Getter @Setter @NoArgsConstructor
public class WebhookConfig extends UuidV7Entity {
    @Id @Column(name = "webhook_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)") private UUID merchantId;
    @Column(name = "callback_url", nullable = false, length = 500) private String callbackUrl;
    @Column(name = "secret_key_hash", length = 255) private String secretKeyHash;
    @Column(name = "event_payment_completed", nullable = false) private Boolean eventPaymentCompleted;
    @Column(name = "event_payment_failed", nullable = false) private Boolean eventPaymentFailed;
    @Column(name = "max_retry", nullable = false) private Integer maxRetry;
    @Column(length = 255) private String description;
    @Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')") private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
