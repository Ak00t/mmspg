package com.ojt_22.mmspg.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.CreateCredentialRequest;
import com.ojt_22.mmspg.dto.CredentialResponse;
import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.ApiCredentialRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiCredentialService {

    private final ApiCredentialRepository credentialRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Unique Client ID ထုတ်ပေးသည့် Method
     */
    public String generateClientId(String environment) {
        String prefix = "SANDBOX".equalsIgnoreCase(environment) ? "mms_test_" : "mms_live_";
        String randomHex = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return prefix + randomHex;
    }

    /**
     * လုံခြုံစိတ်ချရသော 32-byte Base64 Client Secret ထုတ်ပေးသည့် Method
     */
    public String generateClientSecret() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Transactional
    public CredentialResponse createApiCredential(CreateCredentialRequest request) {
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + request.getMerchantId()));

        String rawSecret = generateClientSecret();
        String clientId = generateClientId(request.getEnviroment());

        ApiCredential credential = new ApiCredential();
        credential.setMerchant(merchant);
        credential.setClientId(clientId);
        
        credential.setClientSecretHash(passwordEncoder.encode(rawSecret));
        credential.setIpAddress(request.getIpAddress());
        credential.setEnvironment(request.getEnviroment().toUpperCase());
        credential.setStatus("ACTIVE");
        credential.setKeyName(request.getKeyName());
        credential.setRateLimit(100);
        credential.setExpiresAt(LocalDateTime.now().plusYears(1)); 

        ApiCredential saved = credentialRepository.save(credential);

        return CredentialResponse.builder()
                .credentialId(saved.getId())
                .clientId(saved.getClientId())
                .clientSecret(rawSecret) 
                .environment(saved.getEnvironment())
                .status(saved.getStatus())
                .ipAddress(saved.getIpAddress())
                .expiresAt(saved.getExpiresAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public CredentialResponse regenerateClientSecret(UUID credentialId) {
        ApiCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found with ID: " + credentialId));

        String newRawSecret = generateClientSecret();
        credential.setClientSecretHash(passwordEncoder.encode(newRawSecret));
        credentialRepository.save(credential);

        return CredentialResponse.builder()
                .credentialId(credential.getId())
                .clientId(credential.getClientId())
                .clientSecret(newRawSecret)
                .environment(credential.getEnvironment())
                .status(credential.getStatus())
                .ipAddress(credential.getIpAddress())
                .expiresAt(credential.getExpiresAt())
                .createdAt(credential.getCreatedAt())
                .build();
    }

  
    @Transactional
    public void revokeApiCredential(UUID credentialId) {
        ApiCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found with ID: " + credentialId));

        credential.setStatus("REVOKED");
        credential.setRevokedAt(LocalDateTime.now());
        credentialRepository.save(credential);
    }
}