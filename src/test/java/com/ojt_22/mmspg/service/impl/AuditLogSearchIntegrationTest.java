package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.ojt_22.mmspg.dto.AuditLogResponseDto;
import com.ojt_22.mmspg.service.AuditLogService;

@SpringBootTest
class AuditLogSearchIntegrationTest {

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void testMenuNameCaseInsensitive() {
        Page<AuditLogResponseDto> lower = auditLogService.getAuditLogs("merchant", null, null, null, null, PageRequest.of(0, 15));
        Page<AuditLogResponseDto> upper = auditLogService.getAuditLogs("MERCHANT", null, null, null, null, PageRequest.of(0, 15));
        Page<AuditLogResponseDto> mixed = auditLogService.getAuditLogs("MeRcHaNt", null, null, null, null, PageRequest.of(0, 15));
        Page<AuditLogResponseDto> exact = auditLogService.getAuditLogs("Merchant", null, null, null, null, PageRequest.of(0, 15));

        System.out.println("lower total: " + lower.getTotalElements());
        System.out.println("upper total: " + upper.getTotalElements());
        System.out.println("mixed total: " + mixed.getTotalElements());
        System.out.println("exact total: " + exact.getTotalElements());

        assertEquals(exact.getTotalElements(), lower.getTotalElements(), "lower should match exact");
        assertEquals(exact.getTotalElements(), upper.getTotalElements(), "upper should match exact");
        assertEquals(exact.getTotalElements(), mixed.getTotalElements(), "mixed should match exact");
    }

    @Test
    void testAdminMenuNameCaseInsensitive() {
        Page<AuditLogResponseDto> lower = auditLogService.getAuditLogs("admin", null, null, null, null, PageRequest.of(0, 15));
        Page<AuditLogResponseDto> upper = auditLogService.getAuditLogs("ADMIN", null, null, null, null, PageRequest.of(0, 15));
        Page<AuditLogResponseDto> exact = auditLogService.getAuditLogs("Admin", null, null, null, null, PageRequest.of(0, 15));

        System.out.println("admin lower total: " + lower.getTotalElements());
        System.out.println("admin upper total: " + upper.getTotalElements());

        assertEquals(exact.getTotalElements(), lower.getTotalElements(), "lower should match exact");
        assertEquals(exact.getTotalElements(), upper.getTotalElements(), "upper should match exact");
    }
}
