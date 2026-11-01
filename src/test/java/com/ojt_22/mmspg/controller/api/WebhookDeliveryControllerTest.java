package com.ojt_22.mmspg.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.ojt_22.mmspg.dto.WebhookDeliveryDto;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;
import com.ojt_22.mmspg.service.WebhookDeliveryService;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryControllerTest {

	@Mock
	private WebhookDeliveryService webhookDeliveryService;

	@Mock
	private AuthenticatedMerchantService authenticatedMerchantService;

	private WebhookDeliveryController controller() {
		return new WebhookDeliveryController(webhookDeliveryService, authenticatedMerchantService);
	}

	@Test
	void getDeliveries_ReturnsPageFromService() {
		UUID merchantId = UUID.randomUUID();
		WebhookDeliveryDto dto = WebhookDeliveryDto.builder().deliveryId(UUID.randomUUID())
				.eventType("PAYMENT_COMPLETED").status("DELIVERED").build();
		Page<WebhookDeliveryDto> page = new PageImpl<>(List.of(dto));
		when(webhookDeliveryService.getDeliveriesByMerchant(any(UUID.class), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<WebhookDeliveryDto>> result = controller().getDeliveries(merchantId, 0, 10);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(1, result.getBody().getTotalElements());
		assertEquals("DELIVERED", result.getBody().getContent().get(0).getStatus());
	}

	@Test
	void getDeliveries_ChecksAccessBeforeQueryingService() {
		UUID merchantId = UUID.randomUUID();
		when(webhookDeliveryService.getDeliveriesByMerchant(any(UUID.class), any(Pageable.class)))
				.thenReturn(Page.empty());

		controller().getDeliveries(merchantId, 0, 10);

		InOrder inOrder = Mockito.inOrder(authenticatedMerchantService, webhookDeliveryService);
		inOrder.verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
		inOrder.verify(webhookDeliveryService).getDeliveriesByMerchant(any(UUID.class), any(Pageable.class));
	}

	@Test
	void getDeliveries_BuildsPageableFromParams() {
		UUID merchantId = UUID.randomUUID();
		when(webhookDeliveryService.getDeliveriesByMerchant(any(UUID.class), any(Pageable.class)))
				.thenReturn(Page.empty());

		controller().getDeliveries(merchantId, 2, 50);

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(webhookDeliveryService).getDeliveriesByMerchant(Mockito.eq(merchantId), captor.capture());
		assertEquals(2, captor.getValue().getPageNumber());
		assertEquals(50, captor.getValue().getPageSize());
	}

	@Test
	void getDeliveries_AccessDenied_DoesNotCallService() {
		UUID merchantId = UUID.randomUUID();
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(merchantId);
		WebhookDeliveryController controller = controller();

		assertThrows(AccessDeniedException.class, () -> controller.getDeliveries(merchantId, 0, 10));

		verifyNoInteractions(webhookDeliveryService);
	}

	@Test
	void redeliver_Returns200WithMessage() {
		UUID deliveryId = UUID.randomUUID();

		ResponseEntity<String> result = controller().redeliver(deliveryId);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("Redelivery task triggered successfully.", result.getBody());
		verify(webhookDeliveryService).redeliver(deliveryId);
	}

	@Test
	void redeliver_ServiceThrows_PropagatesException() {
		UUID deliveryId = UUID.randomUUID();
		doThrow(new IllegalArgumentException("Delivery not found")).when(webhookDeliveryService)
				.redeliver(deliveryId);
		WebhookDeliveryController controller = controller();

		assertThrows(IllegalArgumentException.class, () -> controller.redeliver(deliveryId));
	}
}
