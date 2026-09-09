package com.ojt_22.mmspg.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class CredentialUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

   
    private CredentialUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Environment အလိုက် Unique Client ID ထုတ်ပေးခြင်း
     * Sandbox: mms_test_xxxxxxxxxxxxxxxx
     * Production: mms_live_xxxxxxxxxxxxxxxx
     */
    public static String generateClientId(String environment) {
        String prefix = "SANDBOX".equalsIgnoreCase(environment) ? "mms_test_" : "mms_live_";
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return prefix + randomPart;
    }

    
    public static String generateClientSecret() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

   
    public static String generateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }
}