package com.ojt_22.mmspg.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.DashboardSummaryResponse;
import com.ojt_22.mmspg.service.AdminDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Endpoints for Admin Dashboard Analytics")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    // 🔴 ဤနေရာတွင် ADMIN, SUPPORT, AUDITOR အားလုံး ဝင်ကြည့်နိုင်ရန် ပြင်ဆင်လိုက်ပါသည်
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Get Dashboard Summary", description = "Retrieves aggregated statistics and recent requests for the admin dashboard.")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
