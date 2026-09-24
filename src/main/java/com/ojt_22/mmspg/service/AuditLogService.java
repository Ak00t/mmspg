package com.ojt_22.mmspg.service;

import com.ojt_22.mmspg.dto.AuditLogCreateRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

	public void logActivity(AuditLogCreateRequest requestDto, HttpServletRequest request);
}
