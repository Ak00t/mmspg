package com.ojt_22.mmspg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MccCodeRequestDto {
    @NotBlank(message = "MCC Code is required")
    @Size(max = 10, message = "MCC Code cannot exceed 10 characters")
    private String mccCode;

    @NotBlank(message = "MCC Name is required")
    @Size(max = 150, message = "MCC Name cannot exceed 150 characters")
    private String mccName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
