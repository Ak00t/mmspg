package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.ojt_22.mmspg.dto.DailySettlementResponse;
import com.ojt_22.mmspg.dto.SettlementResponse;
import java.util.List;

import com.ojt_22.mmspg.entity.PaymentTransaction;

public interface SettlementService {

    public SettlementResponse processSettlement(UUID transactionId);
    
    public BigDecimal calculateGrossAmount(
            List<PaymentTransaction> transactions);
    
    public BigDecimal calculateTotalFees(
            List<PaymentTransaction> transactions);
    
    public BigDecimal calculateNetSettlement(
            BigDecimal grossAmount,
            BigDecimal totalFees);
    
    public BigDecimal calculateGrossAmountByTransactionIds(
            List<UUID> transactionIds);
    
    public BigDecimal calculateTotalFeesByTransactionIds(
            List<UUID> transactionIds);
    
    public BigDecimal calculateNetSettlementByTransactionIds(
            List<UUID> transactionIds);
    public DailySettlementResponse runDailySettlement();

}