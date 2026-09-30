package com.ojt_22.mmspg.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ojt_22.mmspg.dto.FeeCalculationResponse;
import com.ojt_22.mmspg.dto.FeeConfigRequest;
import com.ojt_22.mmspg.dto.FeeConfigResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantFee;

public interface FeeService {

	public FeeConfigResponse createFeeConfig(FeeConfigRequest request);

	    BigDecimal netAmount =
	            calculateNetAmount(amount, feeAmount);

	    FeeCalculationResponse response =
	            new FeeCalculationResponse();

	    response.setAmount(amount);
	    response.setFeeAmount(feeAmount);
	    response.setNetAmount(netAmount);

	    return response;
	}
}
