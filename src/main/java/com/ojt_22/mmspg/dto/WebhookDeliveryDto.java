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
public class WebhookDeliveryDto {
	
	private UUID deliveryId;
    private UUID transactionId;
    private String eventType;
    private String payload;
    private Integer responseStatus;
    private Integer attemptCount;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private String errorMessage;
    private LocalDateTime nextRetryAt;

}
