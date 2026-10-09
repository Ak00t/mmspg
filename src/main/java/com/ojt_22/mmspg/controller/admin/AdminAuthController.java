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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.dto.AdminAuthResponse;
import com.ojt_22.mmspg.dto.AdminLoginRequest;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.security.CustomStaffDetailsService;
import com.ojt_22.mmspg.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Portal Authentication", description = "Unified Endpoint for Admin and Staff Portal Login")
public class AdminAuthController {

	// ========================================================
	// 🔴 ၂။ ADMIN သီးသန့် လမ်းကြောင်း (Admin မဟုတ်ပါက ပိတ်ချမည်)
	// ========================================================
	@PostMapping("/admin/login")
	@Auditable(menuName = "Admin Authentication", action = "LOGIN", description = "Admin login")
	@Operation(summary = "Login to Admin Portal", description = "Only Admin can login here.")
	public ResponseEntity<AdminAuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest loginRequest) {

    // ========================================================
    // 🔴 Admin နှင့် Staff အားလုံးအတွက် လမ်းကြောင်းတစ်ခုတည်း (Unified Login)
    // ========================================================
    @PostMapping("/login")
    @Operation(summary = "Login to Portal", description = "Allows both Admin and Staff to login with correct password.")
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest loginRequest) {

        UserDetails userDetails;
        try {
            userDetails = customStaffDetailsService.loadUserByUsername(loginRequest.getEmail());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // ၁။ Password မှန်/မမှန် အရင်ဆုံး စစ်ဆေးပါမည်
        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // ၂။ Database မှ သက်ဆိုင်ရာ User ကို ဆွဲထုတ်ပါမည်
        StaffUser staff = staffUserRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // ၃။ Role စစ်ဆေးပါမည် (ADMIN, SUPPORT, AUDITOR တစ်ခုခု ဖြစ်ရပါမည်)
        String roleName = staff.getRole().name();
        if (!roleName.equals("ADMIN") && !roleName.equals("SUPPORT") && !roleName.equals("AUDITOR")) {
            throw new BadCredentialsException("Access Denied: You do not have permission to access this portal.");
        }

        // ၄။ Password လည်းမှန်ကန်ပြီး Role လည်းရှိပါက Token ထုတ်ပေးပါမည်
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

	// ========================================================
	// 🔵 ၃။ STAFF သီးသန့် လမ်းကြောင်း (Staff မဟုတ်ပါက ပိတ်ချမည်)
	// ========================================================
	@PostMapping("/staff/login")
	@Auditable(menuName = "Staff Authentication", action = "LOGIN", description = "Staff login")
	@Operation(summary = "Login to Staff Portal", description = "Only Staff can login here.")
	public ResponseEntity<AdminAuthResponse> staffLogin(@Valid @RequestBody AdminLoginRequest loginRequest) {

        // 🔴 ဤနေရာတွင် roleName ကို နောက်ဆုံးတွင် ထည့်ပေးလိုက်ပါ
        AdminAuthResponse authResponse = new AdminAuthResponse(token, staff.getId(), staff.getFullName(), roleName);
        
        return ResponseEntity.ok(authResponse);
    }

		if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		StaffUser staff = staffUserRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Staff not found"));

		String roleName = staff.getRole().name();
		if (!roleName.equals("SUPPORT") && !roleName.equals("AUDITOR")) {
		    throw new BadCredentialsException("Access Denied: This login portal is ONLY for Staffs!");
		}

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());
		SecurityContextHolder.getContext()
				.setAuthentication(authentication);

		String token = jwtTokenProvider.generateToken(authentication);

		AdminAuthResponse authResponse = new AdminAuthResponse(token, staff.getId(), staff.getFullName());
		return ResponseEntity.ok(authResponse);
	}

	// ========================================================
	// 🟢 ၄။ LOGOUT လမ်းကြောင်း (ယခင် /api/v1/admin/logout အတိုင်း ဖြစ်စေရန်)
	// ========================================================
	@PostMapping("/admin/logout")
	@Auditable(menuName = "Admin Authentication", action = "LOGOUT", description = "Admin logout")
	@Operation(summary = "Logout Admin", description = "Logs out the admin/staff user.")
	public ResponseEntity<?> logout() {
		return ResponseEntity.ok(Map.of("message", "Logged out successfully. Please clear your token in client."));
	}
}
