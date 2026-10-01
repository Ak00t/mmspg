package com.ojt_22.mmspg.service;

import java.util.UUID;

import com.ojt_22.mmspg.dto.SettlementResponse;

public interface SettlementService {

    public SettlementResponse processSettlement(UUID transactionId);

}