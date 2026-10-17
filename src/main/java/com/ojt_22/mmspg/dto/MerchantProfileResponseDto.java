package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProfileResponseDto {
    private UUID id;
    private String merchantCode;
    private String businessName;
    private String businessRegistrationNo;
    private String settlementAccountNo;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private String status;
    private String profileImageUrl;
    private String businessType;
    private LocalDateTime createdAt;
}
