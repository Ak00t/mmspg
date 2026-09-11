package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ojt_22.mmspg.enums.FeeType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeeConfigRequest {

	 private UUID merchantId;

	    private FeeType feeType;

	    private BigDecimal percentageRate;

	    private BigDecimal flatFee;

	    private BigDecimal minimumFee;

	    private BigDecimal maximumFee;

	    private LocalDateTime effectiveFrom;

	    private LocalDateTime effectiveTo;
	
}
