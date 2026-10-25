package com.ojt_22.mmspg.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProfileResponseDto {
	private UUID merchantId;
	private String merchantCode;
	private String businessName;
	private String businessRegistrationNo;
	private String email;
	private String phone;
	private String contactName;
	private String settlementAccountNo;
	private String status;
	private String mccCode;
	private String mccDescription;
}
