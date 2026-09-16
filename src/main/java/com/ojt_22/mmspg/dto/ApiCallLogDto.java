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
public class ApiCallLogDto {
    private UUID logId;
    private String endpoint;
    private String httpMethod;
    private String requestId;
    private String ipAddress;
    private Integer responseStatus;
    private Long responseTimeMs;
    private String errorMessage;
    private LocalDateTime createdAt;
}