package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "payment_transactions") @Getter @Setter @NoArgsConstructor
public class PaymentTransaction extends UuidV7Entity {
    @Id @Column(name = "transaction_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)") private UUID merchantId;
    @Column(name = "branch_id", columnDefinition = "BINARY(16)") private UUID branchId;
    @Column(name = "terminal_id", columnDefinition = "BINARY(16)") private UUID terminalId;
    @Column(name = "order_id", nullable = false, length = 100) private String orderId;
    @Column(name = "transaction_reference", nullable = false, length = 100, unique = true) private String transactionReference;
    @Column(name = "payment_token", nullable = false, length = 255) private String paymentToken;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal amount;
    @Column(nullable = false, length = 3, columnDefinition = "char(3)") private String currency;
    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 4) private BigDecimal feeAmount;
    @Column(name = "net_amount", nullable = false, precision = 18, scale = 4) private BigDecimal netAmount;
    @Column(name = "return_url", length = 500) private String returnUrl;
    @Column(nullable = false, columnDefinition = "enum('INITIATED','PENDING_AUTHORIZATION','COMPLETED','FAILED','REFUNDED','PARTIALLY_REFUNDED')") private String status;
    @Column(name = "failure_reason", length = 500) private String failureReason;
    @Column(name = "initiated_at", nullable = false) private LocalDateTime initiatedAt;
    @Column(name = "authorized_at") private LocalDateTime authorizedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "failed_at") private LocalDateTime failedAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
