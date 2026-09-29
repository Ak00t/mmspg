package com.ojt_22.mmspg.service;

import java.util.UUID;

public interface MerchantLedgerService {

	public void markLedgerAsSettled(UUID ledgerId);

}
