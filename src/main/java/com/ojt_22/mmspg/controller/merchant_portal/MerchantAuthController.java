package com.ojt_22.mmspg.controller.merchant_portal;

import java.time.LocalDateTime;

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

import com.ojt_22.mmspg.dto.JwtAuthResponse;
import com.ojt_22.mmspg.dto.MerchantLoginRequest;
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.security.CustomMerchantDetailsService; // 🔴 ၁။ ဤ Import ကို ထည့်ပါ
import com.ojt_22.mmspg.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/merchant-portal/auth")
@RequiredArgsConstructor
@Tag(name = "Merchant Authentication", description = "Endpoints for Merchant Portal Login")
public class MerchantAuthController {

	// 🔴 ၂။ AuthenticationManager အစား ဤနှစ်ခုကို ထည့်သွင်းပါ
	private final CustomMerchantDetailsService customMerchantDetailsService;
	private final PasswordEncoder passwordEncoder;

	private final JwtTokenProvider jwtTokenProvider;
	private final MerchantRepository merchantRepository;

	@PostMapping("/login")
	@Auditable(menuName = "Merchant Authentication", action = "LOGIN", description = "Merchant login")
	@Operation(summary = "Login to Merchant Portal", description = "Authenticates a merchant by email and password and returns a JWT token.")
	@ApiResponse(responseCode = "200", description = "Successful login")
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials or account inactive")
	public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody MerchantLoginRequest loginRequest) {

		// 🔴 ၃။ Manual ဖြင့် User ရှာဖွေခြင်းနှင့် Password စစ်ဆေးခြင်း
		UserDetails userDetails;
		try {
			userDetails = customMerchantDetailsService.loadUserByUsername(loginRequest.getEmail());
		} catch (UsernameNotFoundException ex) {
			throw new BadCredentialsException("Invalid email or password");
		}

		Merchant merchant = merchantRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		// Check if account is locked
		if (merchant.getLockedUntil() != null) {
			if (merchant.getLockedUntil().isAfter(LocalDateTime.now())) {
				long minutesLeft = java.time.Duration.between(LocalDateTime.now(), merchant.getLockedUntil()).toMinutes() + 1;
				throw new BadCredentialsException("Account is temporarily locked due to 5 failed attempts. Please try again in " + minutesLeft + " minute(s).");
			} else {
				merchant.setLockedUntil(null);
				merchant.setFailedLoginAttempts(0);
				merchantRepository.save(merchant);
			}
		}

		if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
			int attempts = (merchant.getFailedLoginAttempts() != null ? merchant.getFailedLoginAttempts() : 0) + 1;
			if (attempts >= 5) {
				merchant.setFailedLoginAttempts(0);
				merchant.setLockedUntil(LocalDateTime.now().plusMinutes(15));
				merchantRepository.save(merchant);
				throw new BadCredentialsException("Account has been locked due to 5 consecutive failed attempts. Please try again in 15 minutes.");
			} else {
				merchant.setFailedLoginAttempts(attempts);
				merchantRepository.save(merchant);
				throw new BadCredentialsException("Invalid email or password. Attempt " + attempts + " of 5.");
			}
		}

		merchant.setFailedLoginAttempts(0);
		merchant.setLockedUntil(null);
		merchantRepository.save(merchant);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext()
				.setAuthentication(authentication);

		String token = jwtTokenProvider.generateToken(authentication);

		JwtAuthResponse authResponse = new JwtAuthResponse(token, merchant.getId(), merchant.getBusinessName());
		return ResponseEntity.ok(authResponse);
	}
}
