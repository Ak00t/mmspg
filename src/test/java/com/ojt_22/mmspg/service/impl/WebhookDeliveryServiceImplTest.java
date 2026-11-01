package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ojt_22.mmspg.dto.WebhookDeliveryDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.PaymentTransaction;
import com.ojt_22.mmspg.entity.WebhookConfig;
import com.ojt_22.mmspg.entity.WebhookDelivery;
import com.ojt_22.mmspg.enums.WebhookConfigStatus;
import com.ojt_22.mmspg.enums.WebhookDeliveryStatus;
import com.ojt_22.mmspg.repository.WebhookConfigRepository;
import com.ojt_22.mmspg.repository.WebhookDeliveryRepository;
import com.sun.net.httpserver.HttpServer;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceImplTest {

	@Mock
	private WebhookDeliveryRepository webhookDeliveryRepository;
	@Mock
	private WebhookConfigRepository webhookConfigRepository;

	private WebhookDeliveryServiceImpl service;
	private HttpServer server;
	private final AtomicReference<String> receivedEventHeader = new AtomicReference<>();
	private final AtomicReference<String> receivedBody = new AtomicReference<>();

	@BeforeEach
	void setUp() {
		service = new WebhookDeliveryServiceImpl(webhookDeliveryRepository, webhookConfigRepository);
	}

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	private String startServer(int status, String responseBody) throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/hook", exchange -> {
			receivedEventHeader.set(exchange.getRequestHeaders().getFirst("X-Event-Type"));
			receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
			if (bytes.length > 0) {
				exchange.getResponseBody().write(bytes);
			}
			exchange.close();
		});
		server.start();
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/hook";
	}

	private String closedPortUrl() throws IOException {
		String url = startServer(200, "");
		server.stop(0);
		server = null;
		return url;
	}

	private WebhookConfig config(String url, int maxRetry) {
		WebhookConfig config = new WebhookConfig();
		config.setId(UUID.randomUUID());
		config.setCallbackUrl(url);
		config.setMaxRetry(maxRetry);
		config.setEventPaymentCompleted(true);
		config.setEventPaymentFailed(true);
		config.setStatus(WebhookConfigStatus.ACTIVE);
		return config;
	}

	private WebhookDelivery delivery(WebhookConfig config, int attempts, WebhookDeliveryStatus status) {
		WebhookDelivery delivery = new WebhookDelivery();
		delivery.setId(UUID.randomUUID());
		delivery.setWebhook(config);
		delivery.setEventType("PAYMENT_COMPLETED");
		delivery.setPayload("{\"event\":\"PAYMENT_COMPLETED\"}");
		delivery.setAttemptCount(attempts);
		delivery.setStatus(status);
		return delivery;
	}

	private PaymentTransaction transaction(UUID merchantId) {
		Merchant merchant = new Merchant();
		merchant.setId(merchantId);
		PaymentTransaction tx = new PaymentTransaction();
		tx.setId(UUID.randomUUID());
		tx.setMerchant(merchant);
		tx.setAmount(new BigDecimal("1500.00"));
		tx.setCurrency("MMK");
		return tx;
	}

	// ---------- sendWebhook ----------

	@Test
	void sendWebhook_NoActiveConfig_DoesNothing() {
		UUID merchantId = UUID.randomUUID();
		when(webhookConfigRepository.findByMerchantIdAndStatus(merchantId, WebhookConfigStatus.ACTIVE))
				.thenReturn(Optional.empty());

		service.sendWebhook(transaction(merchantId), "PAYMENT_COMPLETED");

		verify(webhookDeliveryRepository, never()).save(any());
	}

	@Test
	void sendWebhook_PaymentCompletedDisabled_DoesNothing() {
		UUID merchantId = UUID.randomUUID();
		WebhookConfig config = config("https://example.com", 3);
		config.setEventPaymentCompleted(false);
		when(webhookConfigRepository.findByMerchantIdAndStatus(merchantId, WebhookConfigStatus.ACTIVE))
				.thenReturn(Optional.of(config));

		service.sendWebhook(transaction(merchantId), "payment_completed");

		verify(webhookDeliveryRepository, never()).save(any());
	}

	@Test
	void sendWebhook_PaymentFailedDisabled_DoesNothing() {
		UUID merchantId = UUID.randomUUID();
		WebhookConfig config = config("https://example.com", 3);
		config.setEventPaymentFailed(false);
		when(webhookConfigRepository.findByMerchantIdAndStatus(merchantId, WebhookConfigStatus.ACTIVE))
				.thenReturn(Optional.of(config));

		service.sendWebhook(transaction(merchantId), "PAYMENT_FAILED");

		verify(webhookDeliveryRepository, never()).save(any());
	}

	@Test
	void sendWebhook_EnabledEvent_SavesPendingDeliveryWithPayload() {
		UUID merchantId = UUID.randomUUID();
		WebhookConfig config = config("https://example.com", 3);
		PaymentTransaction tx = transaction(merchantId);
		when(webhookConfigRepository.findByMerchantIdAndStatus(merchantId, WebhookConfigStatus.ACTIVE))
				.thenReturn(Optional.of(config));
		when(webhookDeliveryRepository.save(any(WebhookDelivery.class))).thenAnswer(inv -> {
			WebhookDelivery d = inv.getArgument(0);
			d.setId(UUID.randomUUID());
			return d;
		});

		service.sendWebhook(tx, "PAYMENT_COMPLETED");

		ArgumentCaptor<WebhookDelivery> captor = ArgumentCaptor.forClass(WebhookDelivery.class);
		verify(webhookDeliveryRepository).save(captor.capture());
		WebhookDelivery saved = captor.getValue();
		assertEquals(config, saved.getWebhook());
		assertEquals(tx, saved.getTransaction());
		assertEquals("PAYMENT_COMPLETED", saved.getEventType());
		assertEquals(0, saved.getAttemptCount());
		assertEquals(WebhookDeliveryStatus.PENDING, saved.getStatus());
		assertNotNull(saved.getNextRetryAt());
		assertTrue(saved.getPayload().contains("PAYMENT_COMPLETED"));
		assertTrue(saved.getPayload().contains(tx.getId().toString()));
		assertTrue(saved.getPayload().contains("MMK"));
	}

	// ---------- executeDelivery ----------

	@Test
	void executeDelivery_NotFound_DoesNothing() {
		UUID id = UUID.randomUUID();
		when(webhookDeliveryRepository.findById(id)).thenReturn(Optional.empty());

		service.executeDelivery(id);

		verify(webhookDeliveryRepository, never()).save(any());
	}

	@Test
	void executeDelivery_AlreadyDelivered_DoesNothing() {
		WebhookDelivery delivery = delivery(config("http://127.0.0.1:1/hook", 3), 1, WebhookDeliveryStatus.DELIVERED);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.executeDelivery(delivery.getId());

		assertEquals(1, delivery.getAttemptCount());
		verify(webhookDeliveryRepository, never()).save(any());
	}

	@Test
	void executeDelivery_Remote2xx_MarksDelivered() throws IOException {
		String url = startServer(200, "OK");
		WebhookDelivery delivery = delivery(config(url, 3), 0, WebhookDeliveryStatus.PENDING);
		delivery.setErrorMessage("previous error");
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.executeDelivery(delivery.getId());

		assertEquals(WebhookDeliveryStatus.DELIVERED, delivery.getStatus());
		assertEquals(1, delivery.getAttemptCount());
		assertEquals(200, delivery.getResponseStatus());
		assertEquals("OK", delivery.getResponseBody());
		assertNotNull(delivery.getSentAt());
		assertNotNull(delivery.getDeliveredAt());
		assertNull(delivery.getErrorMessage());
		assertEquals("PAYMENT_COMPLETED", receivedEventHeader.get());
		assertEquals("{\"event\":\"PAYMENT_COMPLETED\"}", receivedBody.get());
		verify(webhookDeliveryRepository).save(delivery);
	}

	@Test
	void executeDelivery_Remote5xxBelowMaxRetry_StaysPendingWithBackoff() throws IOException {
		String url = startServer(500, "error");
		WebhookDelivery delivery = delivery(config(url, 3), 0, WebhookDeliveryStatus.PENDING);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
		LocalDateTime before = LocalDateTime.now();

		service.executeDelivery(delivery.getId());

		assertEquals(WebhookDeliveryStatus.PENDING, delivery.getStatus());
		assertEquals(1, delivery.getAttemptCount());
		assertEquals(500, delivery.getResponseStatus());
		assertEquals("Remote server responded with HTTP 500", delivery.getErrorMessage());
		// attempt 1 -> 1^2 = 1 minute backoff
		assertTrue(delivery.getNextRetryAt().isAfter(before.plusSeconds(30)));
		assertTrue(delivery.getNextRetryAt().isBefore(before.plusMinutes(2)));
		verify(webhookDeliveryRepository).save(delivery);
	}

	@Test
	void executeDelivery_BackoffGrowsQuadratically() throws IOException {
		String url = startServer(503, "");
		WebhookDelivery delivery = delivery(config(url, 5), 2, WebhookDeliveryStatus.PENDING);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
		LocalDateTime before = LocalDateTime.now();

		service.executeDelivery(delivery.getId());

		// attempt 3 -> 9 minutes
		assertEquals(3, delivery.getAttemptCount());
		assertTrue(delivery.getNextRetryAt().isAfter(before.plusMinutes(8)));
		assertTrue(delivery.getNextRetryAt().isBefore(before.plusMinutes(10)));
	}

	@Test
	void executeDelivery_Remote4xxAtMaxRetry_MarksFailed() throws IOException {
		String url = startServer(404, "");
		WebhookDelivery delivery = delivery(config(url, 3), 2, WebhookDeliveryStatus.PENDING);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.executeDelivery(delivery.getId());

		assertEquals(WebhookDeliveryStatus.FAILED, delivery.getStatus());
		assertEquals(3, delivery.getAttemptCount());
		assertEquals(404, delivery.getResponseStatus());
		assertEquals("Remote server responded with HTTP 404", delivery.getErrorMessage());
		verify(webhookDeliveryRepository).save(delivery);
	}

	@Test
	void executeDelivery_ConnectionFailure_ClearsResponseStatusAndRetries() throws IOException {
		WebhookDelivery delivery = delivery(config(closedPortUrl(), 3), 0, WebhookDeliveryStatus.PENDING);
		delivery.setResponseStatus(200);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.executeDelivery(delivery.getId());

		assertNull(delivery.getResponseStatus());
		assertEquals(WebhookDeliveryStatus.PENDING, delivery.getStatus());
		assertEquals(1, delivery.getAttemptCount());
		assertTrue(delivery.getErrorMessage().startsWith("Connection failed"));
		verify(webhookDeliveryRepository).save(delivery);
	}

	@Test
	void executeDelivery_ConnectionFailureAtMaxRetry_MarksFailed() throws IOException {
		WebhookDelivery delivery = delivery(config(closedPortUrl(), 1), 0, WebhookDeliveryStatus.PENDING);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.executeDelivery(delivery.getId());

		assertEquals(WebhookDeliveryStatus.FAILED, delivery.getStatus());
		assertEquals(1, delivery.getAttemptCount());
	}

	@Test
	void executeDelivery_LongResponseBody_IsTruncated() throws IOException {
		String url = startServer(200, "x".repeat(3000));
		WebhookDelivery delivery = delivery(config(url, 3), 0, WebhookDeliveryStatus.PENDING);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.executeDelivery(delivery.getId());

		assertEquals(2000 + "... [TRUNCATED]".length(), delivery.getResponseBody().length());
		assertTrue(delivery.getResponseBody().endsWith("... [TRUNCATED]"));
	}

	// ---------- getDeliveriesByMerchant ----------

	@Test
	void getDeliveriesByMerchant_MapsEntitiesToDtos() {
		UUID merchantId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);
		PaymentTransaction tx = transaction(merchantId);
		WebhookDelivery delivery = delivery(config("https://example.com", 3), 2, WebhookDeliveryStatus.FAILED);
		delivery.setTransaction(tx);
		delivery.setResponseStatus(500);
		delivery.setErrorMessage("err");
		delivery.setSentAt(LocalDateTime.of(2026, 1, 1, 10, 0));
		delivery.setNextRetryAt(LocalDateTime.of(2026, 1, 1, 10, 5));
		when(webhookDeliveryRepository.findByMerchantId(merchantId, pageable))
				.thenReturn(new PageImpl<>(List.of(delivery), pageable, 1));

		@SuppressWarnings("unchecked")
		Page<WebhookDeliveryDto> result = service.getDeliveriesByMerchant(merchantId, pageable);

		assertEquals(1, result.getTotalElements());
		WebhookDeliveryDto dto = result.getContent().get(0);
		assertEquals(delivery.getId(), dto.getDeliveryId());
		assertEquals(tx.getId(), dto.getTransactionId());
		assertEquals("PAYMENT_COMPLETED", dto.getEventType());
		assertEquals(delivery.getPayload(), dto.getPayload());
		assertEquals(500, dto.getResponseStatus());
		assertEquals(2, dto.getAttemptCount());
		assertEquals("FAILED", dto.getStatus());
		assertEquals("err", dto.getErrorMessage());
		assertEquals(delivery.getSentAt(), dto.getSentAt());
		assertEquals(delivery.getNextRetryAt(), dto.getNextRetryAt());
	}

	@Test
	void getDeliveriesByMerchant_NoDeliveries_ReturnsEmptyPage() {
		UUID merchantId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);
		when(webhookDeliveryRepository.findByMerchantId(merchantId, pageable)).thenReturn(Page.empty(pageable));

		Page<?> result = service.getDeliveriesByMerchant(merchantId, pageable);

		assertTrue(result.isEmpty());
	}

	// ---------- redeliver ----------

	@Test
	void redeliver_NotFound_ThrowsIllegalArgument() {
		UUID id = UUID.randomUUID();
		when(webhookDeliveryRepository.findById(id)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.redeliver(id));

		assertTrue(ex.getMessage().contains(id.toString()));
		verify(webhookDeliveryRepository, never()).save(any());
	}

	@Test
	void redeliver_FailedDelivery_ResetsToPendingAndExecutes() throws IOException {
		String url = startServer(200, "OK");
		WebhookDelivery delivery = delivery(config(url, 3), 3, WebhookDeliveryStatus.FAILED);
		when(webhookDeliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

		service.redeliver(delivery.getId());

		// first save from redeliver (PENDING), second from executeDelivery (DELIVERED)
		verify(webhookDeliveryRepository, times(2)).save(delivery);
		assertEquals(WebhookDeliveryStatus.DELIVERED, delivery.getStatus());
		assertEquals(4, delivery.getAttemptCount());
		assertEquals(200, delivery.getResponseStatus());
	}

	// ---------- retryFailedWebhooks ----------

	@Test
	void retryFailedWebhooks_ExecutesEveryPendingDelivery() {
		WebhookDelivery d1 = delivery(config("https://a.example.com", 3), 1, WebhookDeliveryStatus.PENDING);
		WebhookDelivery d2 = delivery(config("https://b.example.com", 3), 1, WebhookDeliveryStatus.PENDING);
		when(webhookDeliveryRepository.findByStatusAndNextRetryAtBefore(
				org.mockito.ArgumentMatchers.eq(WebhookDeliveryStatus.PENDING), any(LocalDateTime.class)))
				.thenReturn(List.of(d1, d2));
		WebhookDeliveryServiceImpl spied = spy(service);
		doNothing().when(spied).executeDelivery(any(UUID.class));

		spied.retryFailedWebhooks();

		verify(spied).executeDelivery(d1.getId());
		verify(spied).executeDelivery(d2.getId());
	}

	@Test
	void retryFailedWebhooks_NothingPending_DoesNotExecute() {
		when(webhookDeliveryRepository.findByStatusAndNextRetryAtBefore(
				org.mockito.ArgumentMatchers.eq(WebhookDeliveryStatus.PENDING), any(LocalDateTime.class)))
				.thenReturn(List.of());
		WebhookDeliveryServiceImpl spied = spy(service);

		spied.retryFailedWebhooks();

		verify(spied, never()).executeDelivery(any());
	}
}
