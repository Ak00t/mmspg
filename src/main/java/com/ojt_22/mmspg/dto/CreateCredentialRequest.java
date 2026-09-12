package com.ojt_22.mmspg.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CreateCredentialRequest {
	
	private UUID merchantId;
	private String keyName;
	private String ipAddress;
	private String enviroment;

}
