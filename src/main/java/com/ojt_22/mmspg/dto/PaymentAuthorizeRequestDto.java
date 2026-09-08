package com.ojt_22.mmspg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentAuthorizeRequestDto {
    private String paymentToken;
    private String accountNumber;
    private String pinOrOtp;
}