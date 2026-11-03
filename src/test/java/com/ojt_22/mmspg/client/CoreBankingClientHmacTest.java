package com.ojt_22.mmspg.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

public class CoreBankingClientHmacTest {

    @Test
    public void testHmacCalculationMatchesExpected() throws Exception {
        String testData = "POST\n/api/v1/payments/ecommerce/checkout\n1730000000\n{\"test\":\"payload\"}";
        String testSecret = "gateway_super_secret_key_hmac_256_xyz";

        // Reference computation
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(testSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        String expectedHex = HexFormat.of().formatHex(mac.doFinal(testData.getBytes(StandardCharsets.UTF_8)));

        // Test CoreBankingClient method via reflection
        CoreBankingClient client = new CoreBankingClient(null, null);
        Method method = CoreBankingClient.class.getDeclaredMethod("calculateHmacSha256", String.class, String.class);
        method.setAccessible(true);
        String actualHex = (String) method.invoke(client, testData, testSecret);

        assertNotNull(actualHex);
        assertEquals(expectedHex, actualHex);
    }
}
