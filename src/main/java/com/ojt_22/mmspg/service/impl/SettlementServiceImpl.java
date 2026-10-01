package com.ojt_22.mmspg.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

	private final SettlementRepository settlementRepository;
	private final PaymentTransactionRepository paymentTransactionRepository;
	private final StaffUserRepository staffUserRepository;

	public SettlementResponse processSettlement(UUID transactionId) {

		PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

		if (settlementRepository.existsByTransactionId(transactionId)) {
			throw new SettlementAlreadyExistsException("Settlement already exists for transaction: " + transactionId);
		}

		if (transaction.getStatus() != PaymentTransactionStatus.COMPLETED) {
			throw new InvalidTransactionStatusException("Only COMPLETED transaction can be settled");
		}

		String email = SecurityContextHolder.getContext()
				.getAuthentication()
				.getName();

		StaffUser currentAdmin = staffUserRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + email));

		// Settlement object ဆောက်
		Settlement settlement = new Settlement();

		settlement.setCreatedBy(currentAdmin);
		settlement.setTransaction(transaction);
		settlement.setMerchant(transaction.getMerchant());

		settlement.setGrossAmount(transaction.getAmount());
		settlement.setFeeAmount(transaction.getFeeAmount());
		settlement.setNetAmount(transaction.getNetAmount());

		settlement.setSettlementDate(LocalDate.now());

		settlement.setSettlementReference("STL-" + UUID.randomUUID());

		settlement.setBankAccountNo(transaction.getMerchant()
				.getSettlementAccountNo());

		settlement.setStatus(SettlementStatus.COMPLETED);
		settlement.setProcessedAt(LocalDateTime.now());

		// DB ထဲ save
		Settlement savedSettlement = settlementRepository.save(settlement);

		// DTO ဆောက်
		SettlementResponse response = new SettlementResponse();

		response.setSettlementId(savedSettlement.getId());
		response.setTransactionId(savedSettlement.getTransaction()
				.getId());
		response.setMerchantId(savedSettlement.getMerchant()
				.getId());

		response.setSettlementReference(savedSettlement.getSettlementReference());
		response.setSettlementDate(savedSettlement.getSettlementDate());

		response.setGrossAmount(savedSettlement.getGrossAmount());
		response.setFeeAmount(savedSettlement.getFeeAmount());
		response.setNetAmount(savedSettlement.getNetAmount());

		response.setBankAccountNo(savedSettlement.getBankAccountNo());
		response.setStatus(savedSettlement.getStatus());
		response.setProcessedAt(savedSettlement.getProcessedAt());

		return response;
	}

}