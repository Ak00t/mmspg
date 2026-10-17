package com.ojt_22.mmspg.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.ChangePasswordRequestDto;
import com.ojt_22.mmspg.dto.StaffLoginActivityResponseDto;
import com.ojt_22.mmspg.dto.StaffRequestDto;
import com.ojt_22.mmspg.dto.StaffResponseDto;
import com.ojt_22.mmspg.dto.UserProfileResponseDto;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.service.StaffLoginActivityService;
import com.ojt_22.mmspg.service.StaffUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/admin/staff-users")
@RequiredArgsConstructor
@Tag(name = "Admin Staff Management", description = "Endpoints for managing Bank Admins and Staff")
public class AdminStaffController {

    private final StaffUserService staffUserService;
    private final StaffUserRepository staffUserRepository;
    private final StaffLoginActivityService staffLoginActivityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Staff", description = "Retrieves a list of all staff members.")
    public ResponseEntity<List<StaffResponseDto>> getAllStaff() {
        return ResponseEntity.ok(staffUserService.getAllStaffUsersDto());
    }

    @PostMapping
    @Auditable(menuName = "Staff Management", action = "CREATE", targetType = "STAFF", targetId = "#result?.body?.id")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Staff User", description = "Creates a new staff user with encrypted password.")
    public ResponseEntity<?> createStaffUser(@Valid @RequestBody StaffRequestDto request) {
        try {
            StaffResponseDto response = staffUserService.createStaffUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @Auditable(menuName = "Staff Management", action = "UPDATE_STATUS", targetType = "STAFF", targetId = "#id")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle Staff Status", description = "Toggles a staff member's status between ACTIVE and DISABLED.")
    public ResponseEntity<?> toggleStaffStatus(@PathVariable UUID id) {
        try {
            staffUserService.toggleStaffStatus(id);
            return ResponseEntity.ok(Map.of("message", "Staff status toggled successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
   
    @PutMapping("/{id}")
    @Auditable(menuName = "Staff Management", action = "UPDATE", targetType = "STAFF")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Staff User", description = "Updates a staff member's details.")
    public ResponseEntity<?> updateStaffUser(
            @PathVariable UUID id, 
            @RequestBody Map<String, String> request) {
        try {
            String fullName = request.get("fullName");
            String email = request.get("email");
            String role = request.get("role");
            
            staffUserService.updateStaffUser(id, fullName, email, role);
            
            return ResponseEntity.ok(Map.of("message", "Staff user updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================================
    // 🔴 ဤသည်မှာ သင့် Controller တွင် လိုအပ်နေသော Delete လမ်းကြောင်းဖြစ်ပါသည်
    // =====================================================================
    @DeleteMapping("/{id}")
    @Auditable(menuName = "Staff Management", action = "DELETE", targetType = "STAFF")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Staff User", description = "Deletes a staff member permanently.")
    public ResponseEntity<?> deleteStaffUser(@PathVariable UUID id) {
        try {
            staffUserService.deleteStaffUser(id);
            return ResponseEntity.ok(Map.of("message", "Staff user deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Current Staff Profile", description = "Retrieves the profile of the currently logged-in staff user.")
    public ResponseEntity<?> getStaffProfile(Authentication authentication) {
        try {
            UUID staffId = extractLoggedInStaffId(authentication);
            UserProfileResponseDto profile = staffUserService.getStaffProfile(staffId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/profile-picture")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload My Profile Picture", description = "Uploads a profile picture for the logged-in staff.")
    public ResponseEntity<?> uploadMyProfilePicture(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {
        try {
            // အသင့်ရေးထားပြီးဖြစ်သော method ကိုသုံး၍ ID ကို ဆွဲထုတ်ခြင်း
            UUID staffId = extractLoggedInStaffId(authentication);
            
            // Service ကို လှမ်းခေါ်၍ ပုံသိမ်းခြင်း
            String imageUrl = staffUserService.uploadProfilePicture(staffId, file);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture uploaded successfully",
                    "imageUrl", "http://127.0.0.1:8004" + imageUrl
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change Staff Password", description = "Updates password for currently logged-in staff user.")
    public ResponseEntity<?> changeStaffPassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            Authentication authentication) {
        try {
            UUID staffId = extractLoggedInStaffId(authentication);
            staffUserService.changeStaffPassword(staffId, request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/login-activity")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Recent Login Activity", description = "Retrieves the last 4 login activities for the logged-in staff user.")
    public ResponseEntity<?> getRecentLoginActivity(Authentication authentication) {
        try {
            UUID staffId = extractLoggedInStaffId(authentication);
            List<StaffLoginActivityResponseDto> activities = staffLoginActivityService.getRecentActivities(staffId);
            return ResponseEntity.ok(activities);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private UUID extractLoggedInStaffId(Authentication authentication) {
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        String principalName = authentication.getName();
        try {
            return UUID.fromString(principalName);
        } catch (IllegalArgumentException e) {
            return staffUserRepository.findByEmail(principalName)
                    .map(StaffUser::getId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff user not found for email: " + principalName));
        }
    }
}
