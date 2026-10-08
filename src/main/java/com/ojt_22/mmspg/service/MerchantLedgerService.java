package com.ojt_22.mmspg.service;

import java.util.UUID;

import com.ojt_22.mmspg.dto.MerchantLedgerResponse;

public interface MerchantLedgerService {

	public void markLedgerAsSettled(UUID ledgerId);

	public MerchantLedgerResponse createLedgerEntry(UUID transactionId);

}
