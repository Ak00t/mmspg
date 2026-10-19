package com.ojt_22.mmspg.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ojt_22.mmspg.dto.CreateWebhookRequest;
import com.ojt_22.mmspg.dto.UpdateWebhookRequest;
import com.ojt_22.mmspg.dto.WebhookConfigResponse;
import com.ojt_22.mmspg.service.WebhookConfigService;

@ExtendWith(MockitoExtension.class)
class WebhookConfigControllerTest {

	@Mock
	private WebhookConfigService webhookConfigService;

	@InjectMocks
	private WebhookConfigController controller;

	@Test
	void createWebhook_Returns201WithBody() {
		CreateWebhookRequest request = new CreateWebhookRequest();
		request.setCallbackUrl("https://example.com/hook");
		WebhookConfigResponse expected = WebhookConfigResponse.builder().webhookId(UUID.randomUUID())
				.callbackUrl("https://example.com/hook").build();
		when(webhookConfigService.createWebhook(request)).thenReturn(expected);

		ResponseEntity<WebhookConfigResponse> result = controller.createWebhook(request);

		assertEquals(HttpStatus.CREATED, result.getStatusCode());
		assertSame(expected, result.getBody());
	}

	@Test
	void createWebhook_ServiceThrows_PropagatesException() {
		CreateWebhookRequest request = new CreateWebhookRequest();
		when(webhookConfigService.createWebhook(request)).thenThrow(new IllegalArgumentException("bad"));

		assertThrows(IllegalArgumentException.class, () -> controller.createWebhook(request));
	}

	@Test
	void updateWebhook_Returns200WithBody() {
		UUID id = UUID.randomUUID();
		UpdateWebhookRequest request = new UpdateWebhookRequest();
		request.setDescription("updated");
		WebhookConfigResponse expected = WebhookConfigResponse.builder().webhookId(id).description("updated")
				.build();
		when(webhookConfigService.updateWebhook(id, request)).thenReturn(expected);

		ResponseEntity<WebhookConfigResponse> result = controller.updateWebhook(id, request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertSame(expected, result.getBody());
	}

	@Test
	void updateWebhook_ServiceThrows_PropagatesException() {
		UUID id = UUID.randomUUID();
		UpdateWebhookRequest request = new UpdateWebhookRequest();
		when(webhookConfigService.updateWebhook(id, request)).thenThrow(new IllegalArgumentException("not found"));

		assertThrows(IllegalArgumentException.class, () -> controller.updateWebhook(id, request));
	}

	@Test
	void deleteWebhook_Returns200WithMessage() {
		UUID id = UUID.randomUUID();

		ResponseEntity<String> result = controller.deleteWebhook(id);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("Webhook configuration has been deactivated successfully.", result.getBody());
		verify(webhookConfigService).deleteWebhook(id);
	}

	@Test
	void deleteWebhook_ServiceThrows_PropagatesException() {
		UUID id = UUID.randomUUID();
		doThrow(new IllegalArgumentException("not found")).when(webhookConfigService).deleteWebhook(id);

		assertThrows(IllegalArgumentException.class, () -> controller.deleteWebhook(id));
	}

	@Test
	void testWebhook_ReturnsServiceResultInBody() {
		UUID id = UUID.randomUUID();
		when(webhookConfigService.testWebhook(id)).thenReturn("Test Webhook sent. Remote status code: 200");

		ResponseEntity<String> result = controller.testWebhook(id);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("Test Webhook sent. Remote status code: 200", result.getBody());
	}

	@Test
	void testWebhook_ServiceThrows_PropagatesException() {
		UUID id = UUID.randomUUID();
		when(webhookConfigService.testWebhook(id)).thenThrow(new IllegalArgumentException("not found"));

		assertThrows(IllegalArgumentException.class, () -> controller.testWebhook(id));
	}
}
