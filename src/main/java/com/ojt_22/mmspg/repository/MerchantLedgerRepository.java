package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ojt_22.mmspg.entity.MerchantLedgerEntry;

public interface MerchantLedgerRepository extends JpaRepository<MerchantLedgerEntry, UUID> {
	
	List<MerchantLedgerEntry> findByBalanceType(String balanceType);

    // 2. Specific Ledger ID အလိုက် balance_type ကို Update လုပ်ရန်
    @Modifying
    @Query("UPDATE MerchantLedgerEntry m SET m.balanceType = :status WHERE m.id = :ledgerId")
    int updateBalanceType(@Param("ledgerId") UUID ledgerId, @Param("status") String status);
}


