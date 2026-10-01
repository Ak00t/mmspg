package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ojt_22.mmspg.enums.MerchantLedgerEntryBalanceType;
import com.ojt_22.mmspg.enums.MerchantLedgerEntryType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantLedgerResponse {
	private UUID ledgerId;
	private UUID merchantId;
	private UUID transactionId;
	private BigDecimal amount;
	private MerchantLedgerEntryType entryType;
	private MerchantLedgerEntryBalanceType balanceType;
	private String description;
	private LocalDateTime createdAt;

}
