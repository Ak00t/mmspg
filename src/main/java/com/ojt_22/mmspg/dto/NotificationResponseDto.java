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
public class NotificationResponseDto {

	private UUID id;
	private UUID staffId;
	private String title;
	private String message;
	private String type;
	private Boolean isRead;
	private LocalDateTime createdAt;
}
