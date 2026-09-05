package com.ojt_22.mmspg.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "merchant_ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public class MerchantLedgerEntry extends UuidV7Entity {

    @Id
    @Column(name = "ledger_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "merchant_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID merchantId;

    @Column(name = "transaction_id", columnDefinition = "BINARY(16)")
    private UUID transactionId;

    @Column(name = "settlement_id", columnDefinition = "BINARY(16)")
    private UUID settlementId;

    @Column(name = "entry_type", nullable = false, columnDefinition = "enum('DEBIT','CREDIT')")
    private String entryType;

    @Column(name = "balance_type", nullable = false, columnDefinition = "enum('CLEARED','PENDING','HELD')")
    private String balanceType = "PENDING";

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
