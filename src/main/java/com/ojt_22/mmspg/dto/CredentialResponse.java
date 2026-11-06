package com.ojt_22.mmspg.dto;

import java.time.LocalDateTime;
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
public class CredentialResponse {

    private UUID credentialId;
    private String clientId;
    private String clientSecret; 
    private String keyName;
    private String environment;
    private String status;
    private String ipAddress;
    private Integer rateLimit;
    private LocalDateTime lastUsedAt;
    private LocalDateTime revokedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}