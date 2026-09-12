package com.ojt_22.mmspg.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.AdminAuthResponse;
import com.ojt_22.mmspg.dto.AdminLoginRequest;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.security.JwtTokenProvider;
import com.ojt_22.mmspg.security.CustomStaffDetailsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
// 🔴 ၁။ လမ်းကြောင်းများကို ခွဲထုတ်ရန် ဤနေရာတွင် "/api/v1" ဟုသာ ထားပါမည်
@RequestMapping("/api/v1") 
@RequiredArgsConstructor
@Tag(name = "Admin & Staff Authentication", description = "Endpoints for Admin and Staff Portal Login")
public class AdminAuthController {

    private final CustomStaffDetailsService customStaffDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StaffUserRepository staffUserRepository;

    // ========================================================
    // 🔴 ၂။ ADMIN သီးသန့် လမ်းကြောင်း (Admin မဟုတ်ပါက ပိတ်ချမည်)
    // ========================================================
    @PostMapping("/admin/login")
    @Operation(summary = "Login to Admin Portal", description = "Only Admin can login here.")
    public ResponseEntity<AdminAuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest loginRequest) {
        
        UserDetails userDetails;
        try {
            userDetails = customStaffDetailsService.loadUserByUsername(loginRequest.getEmail());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        StaffUser staff = staffUserRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Staff not found"));

        if (!staff.getRole().equals("ADMIN")) {
            throw new BadCredentialsException("Access Denied: This login portal is ONLY for Admins!");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        
        AdminAuthResponse authResponse = new AdminAuthResponse(token, staff.getId(), staff.getFullName());
        return ResponseEntity.ok(authResponse);
    }


    // ========================================================
    // 🔵 ၃။ STAFF သီးသန့် လမ်းကြောင်း (Staff မဟုတ်ပါက ပိတ်ချမည်)
    // ========================================================
    @PostMapping("/staff/login")
    @Operation(summary = "Login to Staff Portal", description = "Only Staff can login here.")
    public ResponseEntity<AdminAuthResponse> staffLogin(@Valid @RequestBody AdminLoginRequest loginRequest) {
        
        UserDetails userDetails;
        try {
            userDetails = customStaffDetailsService.loadUserByUsername(loginRequest.getEmail());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        StaffUser staff = staffUserRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Staff not found"));

        if (!staff.getRole().equals("STAFF")) {
            throw new BadCredentialsException("Access Denied: This login portal is ONLY for Staffs!");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        
        AdminAuthResponse authResponse = new AdminAuthResponse(token, staff.getId(), staff.getFullName());
        return ResponseEntity.ok(authResponse);
    }

    

    // ========================================================
    // 🟢 ၄။ LOGOUT လမ်းကြောင်း (ယခင် /api/v1/admin/logout အတိုင်း ဖြစ်စေရန်)
    // ========================================================
    @PostMapping("/admin/logout")
    @Operation(summary = "Logout Admin", description = "Logs out the admin/staff user.")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(
            Map.of("message", "Logged out successfully. Please clear your token in client.")
        );
    }
}