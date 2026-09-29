package com.ojt_22.mmspg.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCheckoutInfoDto {
    private String businessName;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String status;
}