package com.ojt_22.mmspg.client;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojt_22.mmspg.dto.CoreBankingResponseDto;
import com.ojt_22.mmspg.dto.EcommercePaymentRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreBankingClient {

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${core.banking.service.url:http://localhost:8080/api/v1/payments/ecommerce/checkout}")
	private String coreBankingCheckoutUrl;

	@Value("${core.banking.mock.enabled:true}")
	private boolean mockEnabled;

	@Value("${core.banking.merchant-id:GW-PAYMENT-001}")
	private String merchantId;

	@Value("${core.banking.hmac-secret:gateway_super_secret_key_hmac_256_xyz}")
	private String hmacSecret;

	public CoreBankingResponseDto executeDebit(String sourceAccountNumber, String merchantAccountNumber,
			BigDecimal amount, BigDecimal feeAmount, String orderId, String transactionReference, String description) {

		if (mockEnabled) {
			log.info("Mock Core Banking mode is ACTIVE. Simulating debit for orderId={}, amount={}, fee={}",
					orderId, amount, feeAmount);

			if ("FAIL_ACC".equalsIgnoreCase(sourceAccountNumber)
					|| (amount != null && amount.compareTo(new BigDecimal("100000000")) > 0)) {
				return CoreBankingResponseDto.builder()
						.success(false)
						.coreTransactionRef(null)
						.failureReason("Insufficient account balance or account locked (Mock)")
						.build();
			}

			String mockTxnRef = "CORE-MOCK-" + System.currentTimeMillis();
			log.info("Mock Core Banking debit approved: coreTransactionRef={}", mockTxnRef);
			return CoreBankingResponseDto.builder()
					.success(true)
					.coreTransactionRef(mockTxnRef)
					.failureReason(null)
					.build();
		}

		EcommercePaymentRequestDto request = EcommercePaymentRequestDto.builder()
				.sourceAccountNumber(sourceAccountNumber)
				.merchantAccountNumber(merchantAccountNumber)
				.amount(amount)
				.serviceFee(feeAmount != null ? feeAmount : BigDecimal.ZERO)
				.currency("MMK")
				.orderId(orderId != null ? orderId : transactionReference)
				.idempotencyKey(transactionReference)
				.description(description != null ? description : "Payment for order " + orderId)
				.build();

		try {
			String rawJsonBody = objectMapper.writeValueAsString(request);
			String timestamp = String.valueOf(Instant.now()
					.getEpochSecond());

			// Canonical format: <HTTP_METHOD>\n<REQUEST_URI>\n<X-TIMESTAMP>\n<RAW_BODY>
			// URI is /api/v1/payments/ecommerce/checkout
			String requestUri = extractPathFromUrl(coreBankingCheckoutUrl);
			String canonicalPayload = "POST\n" + requestUri + "\n" + timestamp + "\n" + rawJsonBody;
			String signature = calculateHmacSha256(canonicalPayload, hmacSecret);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Merchant-Id", merchantId);
			headers.set("X-Timestamp", timestamp);
			headers.set("X-Signature", signature);
			headers.set("Idempotency-Key", transactionReference);

			HttpEntity<String> entity = new HttpEntity<>(rawJsonBody, headers);

			log.info("Sending ecommerce payment checkout to Core Banking: URL={}, orderId={}, idempotencyKey={}",
					coreBankingCheckoutUrl, orderId, transactionReference);

			ResponseEntity<String> response = restTemplate.exchange(coreBankingCheckoutUrl, HttpMethod.POST, entity,
					String.class);

			if (response.getStatusCode()
					.is2xxSuccessful() && response.getBody() != null) {
				JsonNode root = objectMapper.readTree(response.getBody());
				String txnRef = root.has("transactionRef") ? root.get("transactionRef")
						.asText() : transactionReference;
				String status = root.has("status") ? root.get("status")
						.asText() : "SUCCESS";

				if ("SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
					return CoreBankingResponseDto.builder()
							.success(true)
							.coreTransactionRef(txnRef)
							.build();
				} else {
					String msg = root.has("message") ? root.get("message")
							.asText() : "Core Banking transaction status: " + status;
					return CoreBankingResponseDto.builder()
							.success(false)
							.failureReason(msg)
							.build();
				}
			}

			return CoreBankingResponseDto.builder()
					.success(false)
					.failureReason("Received empty response from Core Banking")
					.build();

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Core Banking Debit Failed. Status: {}, Response: {}", e.getStatusCode(),
					e.getResponseBodyAsString());
			return CoreBankingResponseDto.builder()
					.success(false)
					.failureReason("Core Banking Authorization Failed: " + e.getResponseBodyAsString())
					.build();
		} catch (Exception e) {
			log.error("Failed to connect to Core Banking service", e);
			return CoreBankingResponseDto.builder()
					.success(false)
					.failureReason("Connection error to Core Banking service: " + e.getMessage())
					.build();
		}
	}

	private String extractPathFromUrl(String url) {
		try {
			java.net.URI uri = java.net.URI.create(url);
			String path = uri.getPath();
			return (path != null && !path.isBlank()) ? path : "/api/v1/payments/ecommerce/checkout";
		} catch (Exception e) {
			return "/api/v1/payments/ecommerce/checkout";
		}
	}

	private String calculateHmacSha256(String data, String secret)
			throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
		mac.init(secretKeySpec);
		byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return HexFormat.of()
				.formatHex(hmacBytes);
	}
}
