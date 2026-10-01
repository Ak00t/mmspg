package com.ojt_22.mmspg.dto;

import java.util.UUID;

import com.ojt_22.mmspg.enums.AuditStatus;
import com.ojt_22.mmspg.enums.SourceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogCreateRequest {
	private UUID actorId;
	private String actorType;
	private String roleName;
	private String permissionUsed;
	private String menuName;
	private String action;
	private String description;
	private String targetType;
	private String targetId;
	private AuditStatus status;
	private SourceType sourceType;
}
