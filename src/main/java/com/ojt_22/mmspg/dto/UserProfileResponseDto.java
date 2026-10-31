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
public class UserProfileResponseDto {
    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private String profileImageUrl;
}
