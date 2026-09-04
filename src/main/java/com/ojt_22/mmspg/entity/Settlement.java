package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "settlements") @Getter @Setter @NoArgsConstructor
public class Settlement extends UuidV7Entity {
    @Id @Column(name = "settlement_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)") private UUID merchantId;
    @Column(name = "settlement_reference", nullable = false, length = 100, unique = true) private String settlementReference;
    @Column(name = "settlement_date", nullable = false) private LocalDate settlementDate;
    @Column(name = "gross_amount", nullable = false, precision = 18, scale = 4) private BigDecimal grossAmount;
    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 4) private BigDecimal feeAmount;
    @Column(name = "refund_amount", nullable = false, precision = 18, scale = 4) private BigDecimal refundAmount;
    @Column(name = "net_amount", nullable = false, precision = 18, scale = 4) private BigDecimal netAmount;
    @Column(name = "bank_account_no", nullable = false, length = 100) private String bankAccountNo;
    @Column(nullable = false, columnDefinition = "enum('PENDING','PROCESSING','COMPLETED','FAILED')") private String status;
    @Column(name = "processed_at") private LocalDateTime processedAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
