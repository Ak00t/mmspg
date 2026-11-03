package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ojt_22.mmspg.enums.FeeType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeeConfigRequest {

	@NotNull(message = "Merchant is required")
	private UUID merchantId;

	@NotNull(message = "Fee Type is required (PERCENTAGE, FLAT, or MIXED)")
	private FeeType feeType;

	@DecimalMin(value = "0.0", inclusive = true, message = "Percentage rate cannot be negative")
	private BigDecimal percentageRate;

	@DecimalMin(value = "0.0", inclusive = true, message = "Flat fee cannot be negative")
	private BigDecimal flatFee;

	@NotNull(message = "Minimum fee is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Minimum fee cannot be negative")
	private BigDecimal minimumFee;

	@NotNull(message = "Maximum fee is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Maximum fee cannot be negative")
	private BigDecimal maximumFee;

	@NotNull(message = "Effective from date is required")
	private LocalDateTime effectiveFrom;

	private LocalDateTime effectiveTo;

}
