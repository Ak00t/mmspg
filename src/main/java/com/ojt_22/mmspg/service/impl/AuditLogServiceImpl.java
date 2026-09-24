package com.ojt_22.mmspg.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.AuditLogCreateRequest;
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

	public void logActivity(AuditLogCreateRequest requestDto, HttpServletRequest request) {
		String ipAddress = (request != null) ? request.getRemoteAddr() : "127.0.0.1";

		AuditLog logEntry = AuditLog.builder()
				.actorId(requestDto.getActorId())
				.actorType(requestDto.getActorType())
				.roleName(requestDto.getRoleName())
				.permissionUsed(requestDto.getPermissionUsed())
				.menuName(requestDto.getMenuName())
				.action(requestDto.getAction())
				.description(requestDto.getDescription())
				.targetType(requestDto.getTargetType())
				.targetId(requestDto.getTargetId())
				.ipAddress(ipAddress)
				.status(requestDto.getStatus() != null ? requestDto.getStatus() : AuditStatus.SUCCESS)
				.sourceType(requestDto.getSourceType() != null ? requestDto.getSourceType() : SourceType.BACKEND_API)
				.build();

		auditLogRepository.save(logEntry);
	}
}
