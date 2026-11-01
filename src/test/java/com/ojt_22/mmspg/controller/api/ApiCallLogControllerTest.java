package com.ojt_22.mmspg.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

import com.ojt_22.mmspg.dto.ApiCallLogDto;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;
import com.ojt_22.mmspg.service.ApiCallLogService;

@ExtendWith(MockitoExtension.class)
class ApiCallLogControllerTest {

	@Mock
	private ApiCallLogService apiCallLogService;

	@Mock
	private AuthenticatedMerchantService authenticatedMerchantService;

	private ApiCallLogController controller() {
		return new ApiCallLogController(apiCallLogService, authenticatedMerchantService);
	}

	@Test
	void getLogsByMerchant_ReturnsPageFromService() {
		UUID merchantId = UUID.randomUUID();
		ApiCallLogDto dto = ApiCallLogDto.builder().logId(UUID.randomUUID()).endpoint("/api/v1/pay").build();
		Page<ApiCallLogDto> page = new PageImpl<>(List.of(dto));
		when(apiCallLogService.getLogsByMerchant(any(UUID.class), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<ApiCallLogDto>> result = controller().getLogsByMerchant(merchantId, 0, 10);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(1, result.getBody().getTotalElements());
		assertEquals("/api/v1/pay", result.getBody().getContent().get(0).getEndpoint());
	}

	@Test
	void getLogsByMerchant_ChecksAccessBeforeQueryingService() {
		UUID merchantId = UUID.randomUUID();
		when(apiCallLogService.getLogsByMerchant(any(UUID.class), any(Pageable.class)))
				.thenReturn(Page.empty());

		controller().getLogsByMerchant(merchantId, 0, 10);

		InOrder inOrder = Mockito.inOrder(authenticatedMerchantService, apiCallLogService);
		inOrder.verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
		inOrder.verify(apiCallLogService).getLogsByMerchant(any(UUID.class), any(Pageable.class));
	}

	@Test
	void getLogsByMerchant_BuildsPageableFromParams() {
		UUID merchantId = UUID.randomUUID();
		when(apiCallLogService.getLogsByMerchant(any(UUID.class), any(Pageable.class)))
				.thenReturn(Page.empty());

		controller().getLogsByMerchant(merchantId, 3, 25);

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(apiCallLogService).getLogsByMerchant(Mockito.eq(merchantId), captor.capture());
		assertEquals(3, captor.getValue().getPageNumber());
		assertEquals(25, captor.getValue().getPageSize());
	}

	@Test
	void getLogsByMerchant_AccessDenied_DoesNotCallService() {
		UUID merchantId = UUID.randomUUID();
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(merchantId);
		ApiCallLogController controller = controller();

		assertThrows(AccessDeniedException.class, () -> controller.getLogsByMerchant(merchantId, 0, 10));

		verifyNoInteractions(apiCallLogService);
		verify(apiCallLogService, never()).getLogsByMerchant(any(), any());
	}
}
