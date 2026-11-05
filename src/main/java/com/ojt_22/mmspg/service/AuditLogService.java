package com.ojt_22.mmspg.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ojt_22.mmspg.dto.AuditLogCreateRequest;
import com.ojt_22.mmspg.dto.AuditLogResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

	void logActivity(AuditLogCreateRequest requestDto, HttpServletRequest request);

	Page<AuditLogResponseDto> getAuditLogs(
			String menuName,
			String action,
			String status,
			LocalDateTime startDate,
			LocalDateTime endDate,
			Pageable pageable);

	List<AuditLogResponseDto> getRecentActivities(int limit);

	com.ojt_22.mmspg.dto.AuditDashboardSummaryDto getAuditDashboardSummary(Integer days);
}

