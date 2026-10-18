package com.ojt_22.mmspg.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLoginActivityResponseDto {
    private UUID id;
    private String timestamp;
    private String ipAddress;
    private String device;
    private String browser;
    private String location;
    private String status;
}
