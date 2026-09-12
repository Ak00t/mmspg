package com.ojt_22.mmspg.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class StaffResponseDto {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private String status;
}
