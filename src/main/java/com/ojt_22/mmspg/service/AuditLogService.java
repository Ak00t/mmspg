package com.ojt_22.mmspg.service;

import java.util.UUID;

import com.ojt_22.mmspg.enums.AuditStatus;
import com.ojt_22.mmspg.enums.SourceType;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

	public void logActivity(UUID actorId, String actorType, String roleName, String permissionUsed, String menuName,
			String action, String description, String targetType, String targetId, AuditStatus status,
			SourceType sourceType, HttpServletRequest request);
}
