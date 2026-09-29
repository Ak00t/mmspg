package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.ojt_22.mmspg.dto.FeeCalculationResponse;
import com.ojt_22.mmspg.dto.FeeConfigRequest;
import com.ojt_22.mmspg.dto.FeeConfigResponse;
import com.ojt_22.mmspg.entity.MerchantFee;

public interface FeeService {

	public FeeConfigResponse createFeeConfig(FeeConfigRequest request);

	public FeeConfigResponse updateFeeConfig(UUID feeId, FeeConfigRequest request);

	public BigDecimal calculateTransactionFee(BigDecimal amount, MerchantFee fee);

	public BigDecimal calculateNetAmount(BigDecimal amount, BigDecimal feeAmount);

	public FeeCalculationResponse calculateFee(UUID feeId, BigDecimal amount);
}
