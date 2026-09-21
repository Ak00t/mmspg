package com.ojt_22.mmspg.service;

import java.util.List;
import java.util.UUID;

import com.ojt_22.mmspg.dto.PaymentAuthorizeRequestDto;
import com.ojt_22.mmspg.dto.PaymentAuthorizeResponseDto;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.dto.PaymentStatusResponseDto;
import com.ojt_22.mmspg.dto.PaymentTransactionSummaryDto;

public interface PaymentTransactionService {
	public PaymentInitiateResponseDto initiateTransaction(PaymentInitiateRequestDto requestDto, String idempotencyKey);

	public PaymentAuthorizeResponseDto authorizeTransaction(PaymentAuthorizeRequestDto requestDto);

	public PaymentStatusResponseDto getTransactionStatus(String transactionReference);

	public List<PaymentTransactionSummaryDto> getMerchantTransactions(UUID merchantId);

}
