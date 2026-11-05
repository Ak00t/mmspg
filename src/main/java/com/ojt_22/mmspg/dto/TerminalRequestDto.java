package com.ojt_22.mmspg.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TerminalRequestDto {
    
    @NotNull(message = "Merchant is required")
    private UUID merchantId;

    @NotNull(message = "Branch is required")
    private UUID branchId;

    @Size(max = 50, message = "Terminal Code cannot exceed 50 characters")
    private String terminalCode;

    @NotBlank(message = "Terminal Name is required")
    @Size(max = 100, message = "Terminal Name cannot exceed 100 characters")
    private String terminalName;

    @NotBlank(message = "Terminal Type is required (PHYSICAL_POS, VIRTUAL_API)")
    private String terminalType;
}
