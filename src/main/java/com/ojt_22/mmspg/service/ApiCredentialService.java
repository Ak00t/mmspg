package com.ojt_22.mmspg.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.CreateCredentialRequest;
import com.ojt_22.mmspg.dto.CredentialResponse;
import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.enums.ApiCredentialEnvironment;
import com.ojt_22.mmspg.enums.ApiCredentialStatus;
import com.ojt_22.mmspg.repository.ApiCredentialRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiCredentialService {

	private final ApiCredentialRepository credentialRepository;
	private final MerchantRepository merchantRepository;
	private final StaffUserRepository staffUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();
	private final AuthenticatedMerchantService authenticatedMerchantService;

	/**
	 * Unique Client ID ထုတ်ပေးသည့် Method
	 */
	public String generateClientId(String environment) {
		String prefix = "SANDBOX".equalsIgnoreCase(environment) ? "mms_test_" : "mms_live_";
		String randomHex = UUID.randomUUID()
				.toString()
				.replace("-", "")
				.substring(0, 16);
		return prefix + randomHex;
	}

	/**
	 * လုံခြုံစိတ်ချရသော 32-byte Base64 Client Secret ထုတ်ပေးသည့် Method
	 */
	public String generateClientSecret() {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(randomBytes);
	}

	@Transactional(readOnly = true)
	public List<CredentialResponse> getMerchantCredentials() {
		UUID authenticatedMerchantId = authenticatedMerchantService.resolveMerchantId();
		return credentialRepository.findByMerchantIdOrderByCreatedAtDesc(authenticatedMerchantId)
				.stream()
				.map(this::toResponseWithoutSecret)
				.toList();
	}

	private CredentialResponse toResponseWithoutSecret(ApiCredential credential) {
		return CredentialResponse.builder()
				.credentialId(credential.getId())
				.clientId(credential.getClientId())
				.clientSecret(null)
				.keyName(credential.getKeyName())
				.environment(credential.getEnvironment()
						.name())
				.status(credential.getStatus()
						.name())
				.ipAddress(credential.getIpAddress())
				.rateLimit(credential.getRateLimit())
				.lastUsedAt(credential.getLastUsedAt())
				.revokedAt(credential.getRevokedAt())
				.expiresAt(credential.getExpiresAt())
				.createdAt(credential.getCreatedAt())
				.build();
	}

	@Transactional
	public CredentialResponse createApiCredential(CreateCredentialRequest request) {
		UUID authenticatedMerchantId = authenticatedMerchantService.resolveMerchantId();
		if (request.getMerchantId() != null && !authenticatedMerchantId.equals(request.getMerchantId())) {
			throw new IllegalArgumentException("Merchant ID does not match authenticated merchant");
		}
		Merchant merchant = merchantRepository.findById(authenticatedMerchantId)
				.orElseThrow(
						() -> new IllegalArgumentException("Merchant not found with ID: " + authenticatedMerchantId));

		String envString = request.getEnvironment() != null ? request.getEnvironment() : request.getEnviroment();
		if (envString == null || envString.isBlank()) {
			envString = "SANDBOX";
		}

		String rawSecret = generateClientSecret();
		String clientId = generateClientId(envString);

		ApiCredential credential = new ApiCredential();
		credential.setMerchant(merchant);
		credential.setClientId(clientId);
		credential.setClientSecretHash(passwordEncoder.encode(rawSecret));
		credential.setIpAddress(request.getIpAddress() != null ? request.getIpAddress() : "0.0.0.0/0");
		credential.setEnvironment(ApiCredentialEnvironment.valueOf(envString.toUpperCase()));
		credential.setStatus(ApiCredentialStatus.ACTIVE);
		credential.setKeyName(request.getKeyName());
		credential.setRateLimit(100);
		credential.setExpiresAt(LocalDateTime.now()
				.plusYears(1));

		ApiCredential saved = credentialRepository.save(credential);

		return CredentialResponse.builder()
				.credentialId(saved.getId())
				.clientId(saved.getClientId())
				.clientSecret(rawSecret)
				.keyName(saved.getKeyName())
				.environment(saved.getEnvironment()
						.name())
				.status(saved.getStatus()
						.name())
				.ipAddress(saved.getIpAddress())
				.rateLimit(saved.getRateLimit())
				.lastUsedAt(saved.getLastUsedAt())
				.revokedAt(saved.getRevokedAt())
				.expiresAt(saved.getExpiresAt())
				.createdAt(saved.getCreatedAt())
				.build();
	}

	@Transactional
	public CredentialResponse regenerateClientSecret(UUID credentialId) {
		ApiCredential credential = credentialRepository.findById(credentialId)
				.orElseThrow(() -> new IllegalArgumentException("Credential not found with ID: " + credentialId));
		authenticatedMerchantService.assertMerchantAccess(credential.getMerchant()
				.getId());

		String newRawSecret = generateClientSecret();
		credential.setClientSecretHash(passwordEncoder.encode(newRawSecret));
		credentialRepository.save(credential);

		return CredentialResponse.builder()
				.credentialId(credential.getId())
				.clientId(credential.getClientId())
				.clientSecret(newRawSecret)
				.keyName(credential.getKeyName())
				.environment(credential.getEnvironment()
						.name())
				.status(credential.getStatus()
						.name())
				.ipAddress(credential.getIpAddress())
				.rateLimit(credential.getRateLimit())
				.lastUsedAt(credential.getLastUsedAt())
				.revokedAt(credential.getRevokedAt())
				.expiresAt(credential.getExpiresAt())
				.createdAt(credential.getCreatedAt())
				.build();
	}

	@Transactional
	public void revokeApiCredential(UUID credentialId) {
		ApiCredential credential = credentialRepository.findById(credentialId)
				.orElseThrow(() -> new IllegalArgumentException("Credential not found with ID: " + credentialId));
		authenticatedMerchantService.assertMerchantAccess(credential.getMerchant()
				.getId());

		credential.setStatus(ApiCredentialStatus.REVOKED);
		credential.setRevokedAt(LocalDateTime.now());
		credentialRepository.save(credential);
	}
}
