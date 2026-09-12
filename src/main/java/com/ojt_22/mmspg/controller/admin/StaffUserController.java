package com.ojt_22.mmspg.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.service.StaffUserService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/staff")
@RequiredArgsConstructor
@Tag(name = "Admin - Staff Management", description = "Endpoints for managing Bank Admin/Staff users")
public class StaffUserController {

    private final StaffUserService staffUserService;

    // Register အစား ဥပမာအားဖြင့် Staff စာရင်းထုတ်ယူသည့် API ဖြင့် အစားထိုးလိုပါက
    @GetMapping("/list")
    @Operation(summary = "Get all Staff Users", description = "Retrieves a list of all staff users.")
    public ResponseEntity<?> getAllStaffUsers() {
        // လိုအပ်သော Logic များကို ဤနေရာတွင် ရေးသားနိုင်ပါသည်
        return ResponseEntity.ok(staffUserService.getAllStaff()); 
     }
  }


