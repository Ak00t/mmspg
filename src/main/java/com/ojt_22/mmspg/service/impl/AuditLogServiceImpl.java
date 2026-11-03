package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.AuditLogCreateRequest;
import com.ojt_22.mmspg.dto.AuditLogResponseDto;
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

	@Override
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

	@Override
	@Transactional(readOnly = true)
	public Page<AuditLogResponseDto> getAuditLogs(
			String menuName,
			String action,
			String status,
			LocalDateTime startDate,
			LocalDateTime endDate,
			Pageable pageable) {

		AuditStatus auditStatus = null;
		if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
			try {
				auditStatus = AuditStatus.valueOf(status.trim().toUpperCase());
			} catch (IllegalArgumentException ignored) {
			}
		}

		Page<AuditLog> page = auditLogRepository.findWithFilters(
				(menuName != null && !menuName.trim().isEmpty()) ? menuName.trim() : null,
				(action != null && !action.trim().isEmpty()) ? action.trim() : null,
				auditStatus,
				startDate,
				endDate,
				pageable);

		return page.map(this::mapToDto);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AuditLogResponseDto> getRecentActivities(int limit) {
		Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), 50));
		return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
				.getContent()
				.stream()
				.map(this::mapToDto)
				.toList();
	}

	private AuditLogResponseDto mapToDto(AuditLog entity) {
		return AuditLogResponseDto.builder()
				.auditId(entity.getAuditId())
				.actorId(entity.getActorId())
				.actorType(entity.getActorType())
				.roleName(entity.getRoleName())
				.permissionUsed(entity.getPermissionUsed())
				.menuName(entity.getMenuName())
				.action(entity.getAction())
				.description(entity.getDescription())
				.targetType(entity.getTargetType())
				.targetId(entity.getTargetId())
				.ipAddress(entity.getIpAddress())
				.status(entity.getStatus())
				.sourceType(entity.getSourceType())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}

