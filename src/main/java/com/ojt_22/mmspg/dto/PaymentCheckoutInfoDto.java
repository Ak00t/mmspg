package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCheckoutInfoDto {
    private String businessName;
    private String merchantName;
    private String merchantAccountNumber;
    private String orderId;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String returnUrl;
}