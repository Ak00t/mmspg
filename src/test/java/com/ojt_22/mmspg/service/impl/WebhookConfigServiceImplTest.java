package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ojt_22.mmspg.dto.CreateWebhookRequest;
import com.ojt_22.mmspg.dto.UpdateWebhookRequest;
import com.ojt_22.mmspg.dto.WebhookConfigResponse;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.WebhookConfig;
import com.ojt_22.mmspg.enums.WebhookConfigStatus;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.WebhookConfigRepository;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;
import com.sun.net.httpserver.HttpServer;

@ExtendWith(MockitoExtension.class)
class WebhookConfigServiceImplTest {

	@Mock
	private WebhookConfigRepository webhookConfigRepository;
	@Mock
	private MerchantRepository merchantRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private AuthenticatedMerchantService authenticatedMerchantService;

	private WebhookConfigServiceImpl service;
	private UUID merchantId;
	private Merchant merchant;
	private HttpServer server;

	@BeforeEach
	void setUp() {
		service = new WebhookConfigServiceImpl(webhookConfigRepository, merchantRepository,
				passwordEncoder, authenticatedMerchantService);
		merchantId = UUID.randomUUID();
		merchant = new Merchant();
		merchant.setId(merchantId);
	}

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	private CreateWebhookRequest createRequest(UUID reqMerchantId) {
		CreateWebhookRequest request = new CreateWebhookRequest();
		request.setMerchantId(reqMerchantId);
		request.setCallbackUrl("https://example.com/hook");
		request.setEventPaymentCompleted(true);
		request.setEventPaymentFailed(false);
		request.setMaxRetry(3);
		request.setDescription("desc");
		return request;
	}

	private WebhookConfig config(UUID ownerId) {
		Merchant owner = new Merchant();
		owner.setId(ownerId);
		WebhookConfig config = new WebhookConfig();
		config.setId(UUID.randomUUID());
		config.setMerchant(owner);
		config.setCallbackUrl("https://old.example.com/hook");
		config.setEventPaymentCompleted(true);
		config.setEventPaymentFailed(true);
		config.setMaxRetry(5);
		config.setDescription("old");
		config.setStatus(WebhookConfigStatus.ACTIVE);
		return config;
	}

	private String startServer(int status) throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/hook", exchange -> {
			exchange.getRequestBody().readAllBytes();
			exchange.sendResponseHeaders(status, -1);
			exchange.close();
		});
		server.start();
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/hook";
	}

	// ---------- createWebhook ----------

	@Test
	void createWebhook_Success_SavesActiveConfigAndReturnsRawSecret() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
		when(passwordEncoder.encode(anyString())).thenReturn("hashed-secret");
		when(webhookConfigRepository.save(any(WebhookConfig.class))).thenAnswer(inv -> {
			WebhookConfig c = inv.getArgument(0);
			c.setId(UUID.randomUUID());
			return c;
		});

		WebhookConfigResponse response = service.createWebhook(createRequest(merchantId));

		assertNotNull(response.getWebhookId());
		assertEquals(merchantId, response.getMerchantId());
		assertEquals("https://example.com/hook", response.getCallbackUrl());
		assertTrue(response.getRawSecretKey().startsWith("whsec_"));
		assertEquals(true, response.getEventPaymentCompleted());
		assertEquals(false, response.getEventPaymentFailed());
		assertEquals(3, response.getMaxRetry());
		assertEquals("desc", response.getDescription());
		assertEquals("ACTIVE", response.getStatus());

		ArgumentCaptor<WebhookConfig> captor = ArgumentCaptor.forClass(WebhookConfig.class);
		verify(webhookConfigRepository).save(captor.capture());
		assertEquals("hashed-secret", captor.getValue().getSecretKeyHash());
		assertEquals(merchant, captor.getValue().getMerchant());
		verify(passwordEncoder).encode(response.getRawSecretKey());
	}

	@Test
	void createWebhook_NullRequestMerchantId_UsesAuthenticatedMerchant() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
		when(passwordEncoder.encode(anyString())).thenReturn("hashed");
		when(webhookConfigRepository.save(any(WebhookConfig.class))).thenAnswer(inv -> inv.getArgument(0));

		WebhookConfigResponse response = service.createWebhook(createRequest(null));

		assertEquals(merchantId, response.getMerchantId());
	}

	@Test
	void createWebhook_MerchantIdMismatch_ThrowsIllegalArgument() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.createWebhook(createRequest(UUID.randomUUID())));

		assertEquals("Merchant ID does not match authenticated merchant", ex.getMessage());
		verify(webhookConfigRepository, never()).save(any());
	}

	@Test
	void createWebhook_MerchantNotFound_ThrowsIllegalArgument() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.createWebhook(createRequest(merchantId)));

		assertTrue(ex.getMessage().startsWith("Merchant not found"));
		verify(webhookConfigRepository, never()).save(any());
	}

	// ---------- updateWebhook ----------

	@Test
	void updateWebhook_AllFields_UpdatesConfigAndMasksSecret() {
		WebhookConfig config = config(merchantId);
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		when(webhookConfigRepository.save(config)).thenReturn(config);

		UpdateWebhookRequest request = new UpdateWebhookRequest();
		request.setCallbackUrl("https://new.example.com/hook");
		request.setEventPaymentCompleted(false);
		request.setEventPaymentFailed(false);
		request.setMaxRetry(2);
		request.setDescription("new");
		request.setStatus("  inactive ");

		WebhookConfigResponse response = service.updateWebhook(config.getId(), request);

		assertEquals("https://new.example.com/hook", response.getCallbackUrl());
		assertEquals(false, response.getEventPaymentCompleted());
		assertEquals(false, response.getEventPaymentFailed());
		assertEquals(2, response.getMaxRetry());
		assertEquals("new", response.getDescription());
		assertEquals("INACTIVE", response.getStatus());
		assertEquals("********", response.getRawSecretKey());
		assertEquals(merchantId, response.getMerchantId());
		verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
	}

	@Test
	void updateWebhook_NullFields_LeavesExistingValuesUntouched() {
		WebhookConfig config = config(merchantId);
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		when(webhookConfigRepository.save(config)).thenReturn(config);

		WebhookConfigResponse response = service.updateWebhook(config.getId(), new UpdateWebhookRequest());

		assertEquals("https://old.example.com/hook", response.getCallbackUrl());
		assertEquals(true, response.getEventPaymentCompleted());
		assertEquals(true, response.getEventPaymentFailed());
		assertEquals(5, response.getMaxRetry());
		assertEquals("old", response.getDescription());
		assertEquals("ACTIVE", response.getStatus());
	}

	@Test
	void updateWebhook_InvalidStatus_ThrowsIllegalArgument() {
		WebhookConfig config = config(merchantId);
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		UpdateWebhookRequest request = new UpdateWebhookRequest();
		request.setStatus("BOGUS");

		assertThrows(IllegalArgumentException.class, () -> service.updateWebhook(config.getId(), request));

		verify(webhookConfigRepository, never()).save(any());
	}

	@Test
	void updateWebhook_NotFound_ThrowsIllegalArgument() {
		UUID id = UUID.randomUUID();
		when(webhookConfigRepository.findById(id)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.updateWebhook(id, new UpdateWebhookRequest()));

		assertTrue(ex.getMessage().contains(id.toString()));
	}

	@Test
	void updateWebhook_AccessDenied_DoesNotSave() {
		WebhookConfig config = config(UUID.randomUUID());
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(config.getMerchant().getId());

		assertThrows(AccessDeniedException.class,
				() -> service.updateWebhook(config.getId(), new UpdateWebhookRequest()));

		verify(webhookConfigRepository, never()).save(any());
	}

	// ---------- deleteWebhook ----------

	@Test
	void deleteWebhook_SetsStatusInactive() {
		WebhookConfig config = config(merchantId);
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));

		service.deleteWebhook(config.getId());

		assertEquals(WebhookConfigStatus.INACTIVE, config.getStatus());
		verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
		verify(webhookConfigRepository).save(config);
	}

	@Test
	void deleteWebhook_NotFound_ThrowsIllegalArgument() {
		UUID id = UUID.randomUUID();
		when(webhookConfigRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.deleteWebhook(id));

		verify(webhookConfigRepository, never()).save(any());
	}

	@Test
	void deleteWebhook_AccessDenied_KeepsStatusActive() {
		WebhookConfig config = config(UUID.randomUUID());
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(config.getMerchant().getId());

		assertThrows(AccessDeniedException.class, () -> service.deleteWebhook(config.getId()));

		assertEquals(WebhookConfigStatus.ACTIVE, config.getStatus());
		verify(webhookConfigRepository, never()).save(any());
	}

	// ---------- testWebhook ----------

	@Test
	void testWebhook_NotFound_ThrowsIllegalArgument() {
		UUID id = UUID.randomUUID();
		when(webhookConfigRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.testWebhook(id));
	}

	@Test
	void testWebhook_AccessDenied_ThrowsAccessDenied() {
		WebhookConfig config = config(UUID.randomUUID());
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(config.getMerchant().getId());

		assertThrows(AccessDeniedException.class, () -> service.testWebhook(config.getId()));
	}

	@Test
	void testWebhook_RemoteReturns200_ReportsStatusCode() throws IOException {
		WebhookConfig config = config(merchantId);
		config.setCallbackUrl(startServer(200));
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));

		String result = service.testWebhook(config.getId());

		assertEquals("Test Webhook sent. Remote status code: 200", result);
	}

	@Test
	void testWebhook_RemoteReturns500_ReportsStatusCode() throws IOException {
		WebhookConfig config = config(merchantId);
		config.setCallbackUrl(startServer(500));
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));

		String result = service.testWebhook(config.getId());

		assertEquals("Test Webhook sent. Remote status code: 500", result);
	}

	@Test
	void testWebhook_ConnectionFails_ReturnsFailureMessage() throws IOException {
		// Start and immediately stop a server so the port is guaranteed to be closed.
		String url = startServer(200);
		server.stop(0);
		server = null;
		WebhookConfig config = config(merchantId);
		config.setCallbackUrl(url);
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));

		String result = service.testWebhook(config.getId());

		assertTrue(result.startsWith("Test Webhook failed to connect"));
	}

	@Test
	void testWebhook_InvalidUrl_ReturnsFailureMessage() {
		WebhookConfig config = config(merchantId);
		config.setCallbackUrl("not a valid url");
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));

		String result = service.testWebhook(config.getId());

		assertTrue(result.startsWith("Test Webhook failed to connect"));
	}

	// ---------- getMerchantWebhook ----------

	@Test
	void getMerchantWebhook_ReturnsActiveConfigWithMaskedSecret() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		WebhookConfig config = config(merchantId);
		when(webhookConfigRepository.findFirstByMerchantIdOrderByCreatedAtDesc(merchantId))
				.thenReturn(Optional.of(config));

		WebhookConfigResponse response = service.getMerchantWebhook();

		assertNotNull(response);
		assertEquals(config.getId(), response.getWebhookId());
		assertEquals(merchantId, response.getMerchantId());
		assertNull(response.getRawSecretKey());
	}

	@Test
	void getMerchantWebhook_NoConfigFound_ReturnsNull() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(webhookConfigRepository.findFirstByMerchantIdOrderByCreatedAtDesc(merchantId))
				.thenReturn(Optional.empty());

		WebhookConfigResponse response = service.getMerchantWebhook();

		assertNull(response);
	}

	// ---------- regenerateWebhookSecret ----------

	@Test
	void regenerateWebhookSecret_Success_ReturnsNewSecret() {
		WebhookConfig config = config(merchantId);
		when(webhookConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
		when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
		when(webhookConfigRepository.save(any(WebhookConfig.class))).thenAnswer(inv -> inv.getArgument(0));

		WebhookConfigResponse response = service.regenerateWebhookSecret(config.getId());

		assertNotNull(response);
		assertTrue(response.getRawSecretKey().startsWith("whsec_"));
		assertEquals("new-hash", config.getSecretKeyHash());
		verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
	}
}
