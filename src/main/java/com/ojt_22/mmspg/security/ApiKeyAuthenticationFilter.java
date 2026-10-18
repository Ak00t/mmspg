package com.ojt_22.mmspg.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.enums.ApiCredentialStatus;
import com.ojt_22.mmspg.repository.ApiCredentialRepository;
import com.ojt_22.mmspg.utils.ApiLogUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	private final ApiCredentialRepository credentialRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String clientId = request.getHeader("X-Client-ID");
		String clientSecret = request.getHeader("X-Client-Secret");

		// If API key headers are present, attempt API key authentication
		if (StringUtils.hasText(clientId)) {
			if (!StringUtils.hasText(clientSecret)) {
				sendUnauthorizedError(response, "Missing X-Client-Secret header");
				return;
			}

			ApiCredential credential = credentialRepository
					.findByClientIdAndStatus(clientId.trim(), ApiCredentialStatus.ACTIVE)
					.orElse(null);

			if (credential == null || !passwordEncoder.matches(clientSecret.trim(), credential.getClientSecretHash())) {
				log.warn("Invalid API credentials attempted for Client ID: {}", clientId);
				sendUnauthorizedError(response, "Invalid API Client ID or Secret");
				return;
			}

			if (credential.getExpiresAt() != null && credential.getExpiresAt()
					.isBefore(LocalDateTime.now())) {
				log.warn("Expired API credential used for Client ID: {}", clientId);
				sendUnauthorizedError(response, "API credential has expired");
				return;
			}

			// IP check (if specific IP configured, allow loopback or matching IP)
			String clientIp = ApiLogUtils.getClientIp(request);
			String configuredIp = credential.getIpAddress();
			if (StringUtils.hasText(configuredIp) && !isIpAllowed(configuredIp, clientIp)) {
				log.warn("IP whitelist mismatch for Client ID {}: clientIp={}, configuredIp={}", clientId, clientIp,
						configuredIp);
				sendUnauthorizedError(response, "Client IP is not authorized for this API credential");
				return;
			}

			// Successfully authenticated as Merchant via API key
			String merchantId = credential.getMerchant()
					.getId()
					.toString();
			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"));

			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					merchantId, null, authorities);
			authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext()
					.setAuthentication(authenticationToken);

			try {
				credential.setLastUsedAt(LocalDateTime.now());
				credentialRepository.save(credential);
			} catch (Exception e) {
				log.error("Failed to update lastUsedAt for credential {}: {}", credential.getId(), e.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isIpAllowed(String configuredIp, String clientIp) {
		if ("127.0.0.1".equals(configuredIp) || "0.0.0.0".equals(configuredIp) || "*".equals(configuredIp)) {
			return true;
		}
		if (configuredIp.equalsIgnoreCase(clientIp)) {
			return true;
		}
		// Support loopback ipv6 / ipv4 equivalency
		if (("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) && "127.0.0.1".equals(configuredIp)) {
			return true;
		}
		return false;
	}

	private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter()
				.write("{\"error\": \"" + message + "\"}");
	}
}
