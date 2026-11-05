package com.ojt_22.mmspg.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ojt_22.mmspg.security.ApiKeyAuthenticationFilter;
import com.ojt_22.mmspg.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		// 1. Exported Configuration for Customer Checkout & External Frontends
		CorsConfiguration exportedPaymentConfig = new CorsConfiguration();
		exportedPaymentConfig.setAllowedOrigins(Arrays.asList(
				"http://localhost:5173", "http://127.0.0.1:5173",
				"http://localhost:5174", "http://127.0.0.1:5174",
				"http://localhost:5175", "http://127.0.0.1:5175",
				"http://localhost:3000", "http://127.0.0.1:3000"));
		exportedPaymentConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		exportedPaymentConfig.setAllowedHeaders(Arrays.asList(
				"Authorization", "Content-Type", "X-Requested-With", "Accept",
				"Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
				"X-Client-ID", "X-Client-Secret", "Idempotency-Key", "X-Client-Type", "x-client-type",
				"X-Internal-Service-Key", "*"));
		exportedPaymentConfig
				.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
		exportedPaymentConfig.setAllowCredentials(true);
		exportedPaymentConfig.setMaxAge(3600L);

		// 2. Strict Internal Configuration for MMSPG Back-office & Merchant Portal
		CorsConfiguration internalMmspgConfig = new CorsConfiguration();
		internalMmspgConfig.setAllowedOrigins(Arrays.asList("http://localhost:5175", "http://127.0.0.1:5175"));
		internalMmspgConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		internalMmspgConfig.setAllowedHeaders(Arrays.asList(
				"Authorization", "Content-Type", "X-Requested-With", "Accept",
				"Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
				"X-Client-ID", "X-Client-Secret", "Idempotency-Key", "X-Client-Type", "x-client-type",
				"X-Internal-Service-Key", "*"));
		internalMmspgConfig
				.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
		internalMmspgConfig.setAllowCredentials(true);
		internalMmspgConfig.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/v1/payments/**", exportedPaymentConfig);
		source.registerCorsConfiguration("/**", internalMmspgConfig);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize.requestMatchers("/uploads/**").permitAll()
						.requestMatchers("/api/v1/auth/**").permitAll()
						.requestMatchers("/api/v1/merchant-portal/auth/**").permitAll()
						.requestMatchers("/api/v1/login", "/api/v1/merchant-portal/auth/login").permitAll()
						.requestMatchers("/error")
						.permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.permitAll()
						// Customer Portal (Token-based checkout & authorization)
						.requestMatchers("/api/v1/payments/checkout-info/**", "/api/v1/payments/authorize",
								"/mock-checkout.html", "/favicon.ico")
						.permitAll()
						// Mock Core Banking Debit API
						.requestMatchers("/api/v1/core/**")
						.permitAll()
						// Payment endpoints (Merchant API key or portal users)
						.requestMatchers("/api/v1/payments/**")
						.hasAnyRole("API_CLIENT", "ADMIN", "STAFF", "MERCHANT")
						// Admin endpoints (Only ADMIN, SUPPORT, AUDITOR)
						.requestMatchers("/api/v1/admin/**")
						.hasAnyRole("ADMIN", "SUPPORT", "AUDITOR")
						.anyRequest()
						.authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
