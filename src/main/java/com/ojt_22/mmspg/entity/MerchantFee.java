package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "merchant_fees") @Getter @Setter @NoArgsConstructor
public class MerchantFee extends UuidV7Entity {
    @Id @Column(name = "fee_id", columnDefinition = "BINARY(16)") private UUID id;
    @Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)") private UUID merchantId;
    @Column(name = "fee_type", nullable = false, columnDefinition = "enum('PERCENTAGE','FLAT','MIXED')") private String feeType;
    @Column(name = "percentage_rate", precision = 5, scale = 2) private BigDecimal percentageRate;
    @Column(name = "flat_fee", precision = 18, scale = 3) private BigDecimal flatFee;
    @Column(name = "effective_from", nullable = false) private LocalDateTime effectiveFrom;
    @Column(name = "effective_to") private LocalDateTime effectiveTo;
    @Column(nullable = false, columnDefinition = "enum('ACTIVE','INACTIVE')") private String status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "created_by", columnDefinition = "BINARY(16)") private UUID createdBy;
    @Column(name = "updated_by", columnDefinition = "BINARY(16)") private UUID updatedBy;
}
