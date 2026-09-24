package com.ojt_22.mmspg.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.entity.AuditLog;
import com.ojt_22.mmspg.enums.AuditStatus;
import com.ojt_22.mmspg.enums.SourceType;
import com.ojt_22.mmspg.repository.AuditLogRepository;
import com.ojt_22.mmspg.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public void logActivity(UUID actorId, String actorType, String roleName, String permissionUsed, String menuName,
			String action, String description, String targetType, String targetId, AuditStatus status,
			SourceType sourceType, HttpServletRequest request) {
		String ipAddress = (request != null) ? request.getRemoteAddr() : "127.0.0.1";

		AuditLog logEntry = AuditLog.builder()
				.actorId(actorId)
				.actorType(actorType)
				.roleName(roleName)
				.permissionUsed(permissionUsed)
				.menuName(menuName)
				.action(action)
				.description(description)
				.targetType(targetType)
				.targetId(targetId)
				.ipAddress(ipAddress)
				.status(status != null ? status : AuditStatus.SUCCESS)
				.sourceType(sourceType != null ? sourceType : SourceType.BACKEND_API)
				.build();

		auditLogRepository.save(logEntry);
	}
}
