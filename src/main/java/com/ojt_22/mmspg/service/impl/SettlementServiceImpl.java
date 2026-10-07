package com.ojt_22.mmspg.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.dto.DailySettlementResponse;
import com.ojt_22.mmspg.dto.SettlementResponse;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.entity.Settlement;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.enums.PaymentTransactionStatus;
import com.ojt_22.mmspg.enums.SettlementStatus;
import com.ojt_22.mmspg.exception.InvalidTransactionStatusException;
import com.ojt_22.mmspg.exception.ResourceNotFoundException;
import com.ojt_22.mmspg.exception.SettlementAlreadyExistsException;
import com.ojt_22.mmspg.repository.PaymentTransactionRepository;
import com.ojt_22.mmspg.repository.SettlementRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.service.SettlementService;
import java.util.List;
@Service
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StaffUserRepository staffUserRepository;

    public SettlementServiceImpl(
            SettlementRepository settlementRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            StaffUserRepository staffUserRepository) {

        this.settlementRepository = settlementRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public SettlementResponse processSettlement(UUID transactionId) {

        PaymentTransaction transaction =
                paymentTransactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found: " + transactionId));

        if (settlementRepository.existsByTransactionId(transactionId)) {
            throw new SettlementAlreadyExistsException(
                    "Settlement already exists for transaction: " + transactionId);
        }

        if (transaction.getStatus() != PaymentTransactionStatus.COMPLETED) {
            throw new InvalidTransactionStatusException(
                    "Only COMPLETED transaction can be settled");
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        StaffUser currentAdmin = staffUserRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Staff user not found: " + email));

        Settlement settlement = new Settlement();

        settlement.setCreatedBy(currentAdmin);
        settlement.setTransaction(transaction);
        settlement.setMerchant(transaction.getMerchant());

        settlement.setGrossAmount(transaction.getAmount());
        settlement.setFeeAmount(transaction.getFeeAmount());
        settlement.setNetAmount(transaction.getNetAmount());

        settlement.setSettlementDate(LocalDate.now());

        settlement.setSettlementReference(
                "STL-" + UUID.randomUUID());

        settlement.setBankAccountNo(
                transaction.getMerchant().getSettlementAccountNo());

        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setProcessedAt(LocalDateTime.now());

        Settlement savedSettlement =
                settlementRepository.save(settlement);

        SettlementResponse response = new SettlementResponse();

        response.setSettlementId(savedSettlement.getId());
        response.setTransactionId(
                savedSettlement.getTransaction().getId());
        response.setMerchantId(
                savedSettlement.getMerchant().getId());

        response.setSettlementReference(
                savedSettlement.getSettlementReference());
        response.setSettlementDate(
                savedSettlement.getSettlementDate());

        response.setGrossAmount(
                savedSettlement.getGrossAmount());
        response.setFeeAmount(
                savedSettlement.getFeeAmount());
        response.setNetAmount(
                savedSettlement.getNetAmount());

        response.setBankAccountNo(
                savedSettlement.getBankAccountNo());
        response.setStatus(
                savedSettlement.getStatus());
        response.setProcessedAt(
                savedSettlement.getProcessedAt());

        return response;
    }
    
    @Override
    public BigDecimal calculateGrossAmount(
            List<PaymentTransaction> transactions) {

        BigDecimal grossAmount = BigDecimal.ZERO;

        for (PaymentTransaction transaction : transactions) {
            grossAmount = grossAmount.add(transaction.getAmount());
        }

        return grossAmount;
             
    }
    
    @Override
    public BigDecimal calculateTotalFees(
            List<PaymentTransaction> transactions) {

        BigDecimal totalFees = BigDecimal.ZERO;

        for (PaymentTransaction transaction : transactions) {
            totalFees = totalFees.add(transaction.getFeeAmount());
        }

        return totalFees;
    }
    
    @Override
    public BigDecimal calculateNetSettlement(
            BigDecimal grossAmount,
            BigDecimal totalFees) {

        return grossAmount.subtract(totalFees);
    }
    
    
    
    @Override
    public BigDecimal calculateGrossAmountByTransactionIds(
            List<UUID> transactionIds) {

        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findAllById(transactionIds);

        return calculateGrossAmount(transactions);
    }
    
    @Override
    public BigDecimal calculateTotalFeesByTransactionIds(
            List<UUID> transactionIds) {

        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findAllById(transactionIds);

        return calculateTotalFees(transactions);
    }
    
    @Override
    public BigDecimal calculateNetSettlementByTransactionIds(
            List<UUID> transactionIds) {

        List<PaymentTransaction> transactions =
                paymentTransactionRepository.findAllById(transactionIds);

        BigDecimal grossAmount =
                calculateGrossAmount(transactions);

        BigDecimal totalFees =
                calculateTotalFees(transactions);

        return calculateNetSettlement(grossAmount, totalFees);
    }
    
    @Override
    public DailySettlementResponse runDailySettlement() {

        LocalDate today = LocalDate.now();

        LocalDateTime startDate = today.atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        List<PaymentTransaction> transactions =
                paymentTransactionRepository
                        .findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                PaymentTransactionStatus.COMPLETED,
                                startDate,
                                endDate);
        BigDecimal grossAmount =
                calculateGrossAmount(transactions);

        BigDecimal totalFees =
                calculateTotalFees(transactions);

        BigDecimal netSettlement =
                calculateNetSettlement(grossAmount, totalFees);
        
        for (PaymentTransaction transaction : transactions) {

            if (!settlementRepository.existsByTransactionId(transaction.getId())) {
                processSettlement(transaction.getId());
            }           
        }
        DailySettlementResponse response = new DailySettlementResponse();

        response.setSettlementDate(today);
        response.setTransactionCount(transactions.size());
        response.setGrossAmount(grossAmount);
        response.setTotalFees(totalFees);
        response.setNetSettlement(netSettlement);

        return response;
    }
    
}