package com.ojt_22.mmspg.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.AuditLogResponseDto;
import com.ojt_22.mmspg.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogs(
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponseDto> logs = auditLogService.getAuditLogs(menuName, action, status, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLogResponseDto>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<AuditLogResponseDto> recentLogs = auditLogService.getRecentActivities(limit);
        return ResponseEntity.ok(recentLogs);
    }

    @GetMapping("/summary")
    public ResponseEntity<com.ojt_22.mmspg.dto.AuditDashboardSummaryDto> getAuditDashboardSummary(
            @RequestParam(required = false, defaultValue = "7") Integer days
    ) {
        return ResponseEntity.ok(auditLogService.getAuditDashboardSummary(days));
    }
}
