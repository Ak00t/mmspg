package com.ojt_22.mmspg.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BranchRequestDto {
    
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;

    @NotBlank(message = "Branch Code is required")
    @Size(max = 50, message = "Branch Code cannot exceed 50 characters")
    private String branchCode;

    @NotBlank(message = "Branch Name is required")
    @Size(max = 150, message = "Branch Name cannot exceed 150 characters")
    private String branchName;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 30, message = "Phone cannot exceed 30 characters")
    private String phone;
    
    private String address;
}
