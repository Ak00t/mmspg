package com.ojt_22.mmspg.controller.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.ApiCallLogDto;
import com.ojt_22.mmspg.service.ApiCallLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class ApiCallLogController {

    private final ApiCallLogService apiCallLogService;

    
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Page<ApiCallLogDto>> getLogsByMerchant(
            @PathVariable UUID merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiCallLogDto> logs = apiCallLogService.getLogsByMerchant(merchantId, pageable);
        return ResponseEntity.ok(logs);
    }
}