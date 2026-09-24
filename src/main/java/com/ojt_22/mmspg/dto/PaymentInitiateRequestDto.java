package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentInitiateRequestDto {
	
	@NotNull(message = "Merchant ID must not be null")
    private UUID merchantId;
	
    private UUID branchId;
    
    private UUID terminalId;
    
    @NotBlank(message = "Order ID must not be blank")
    private String orderId;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotBlank
    private String currency;
}