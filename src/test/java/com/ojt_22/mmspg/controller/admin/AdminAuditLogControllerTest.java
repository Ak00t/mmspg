package com.ojt_22.mmspg.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ojt_22.mmspg.dto.AuditLogResponseDto;
import com.ojt_22.mmspg.enums.AuditStatus;
import com.ojt_22.mmspg.enums.SourceType;
import com.ojt_22.mmspg.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class AdminAuditLogControllerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminAuditLogController controller;

    @Test
    void getAuditLogs_ReturnsPaginatedList() {
        AuditLogResponseDto dto = AuditLogResponseDto.builder()
                .auditId(1L)
                .actorId(UUID.randomUUID())
                .actorType("STAFF")
                .roleName("ADMIN")
                .menuName("Merchant Approval")
                .action("APPROVE")
                .status(AuditStatus.SUCCESS)
                .sourceType(SourceType.BACKEND_API)
                .createdAt(LocalDateTime.now())
                .build();

        Page<AuditLogResponseDto> pageResult = new PageImpl<>(List.of(dto), PageRequest.of(0, 15), 1);
        when(auditLogService.getAuditLogs(eq("Merchant Approval"), eq("APPROVE"), eq("SUCCESS"), any(), any(), any()))
                .thenReturn(pageResult);

        ResponseEntity<Page<AuditLogResponseDto>> response = controller.getAuditLogs(
                "Merchant Approval", "APPROVE", "SUCCESS", null, null, 0, 15);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Merchant Approval", response.getBody().getContent().get(0).getMenuName());
    }

    @Test
    void getRecentActivities_ReturnsTopList() {
        AuditLogResponseDto dto = AuditLogResponseDto.builder()
                .auditId(2L)
                .menuName("Branch Management")
                .action("CREATE")
                .build();

        when(auditLogService.getRecentActivities(5)).thenReturn(List.of(dto));

        ResponseEntity<List<AuditLogResponseDto>> response = controller.getRecentActivities(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(auditLogService).getRecentActivities(5);
    }
}
