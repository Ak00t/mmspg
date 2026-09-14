package com.ojt_22.mmspg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantRegistrationRequest {

    @NotBlank(message = "Merchant Code is required")
    @Size(max = 50, message = "Merchant Code cannot exceed 50 characters")
    private String merchantCode;

    @NotBlank(message = "Business Name is required")
    @Size(max = 200, message = "Business Name cannot exceed 200 characters")
    private String businessName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String rawPassword;

    @NotNull(message = "MCC ID is required")
    private Integer mccId;

    @NotBlank(message = "Settlement Account No is required")
    @Size(max = 100, message = "Settlement Account No cannot exceed 100 characters")
    private String settlementAccountNo;

    @Size(max = 150, message = "Contact Name cannot exceed 150 characters")
    private String contactName;

    @Size(max = 30, message = "Phone cannot exceed 30 characters")
    private String phone;

    private String address;
}
