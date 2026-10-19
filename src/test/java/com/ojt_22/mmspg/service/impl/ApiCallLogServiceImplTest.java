package com.ojt_22.mmspg.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ojt_22.mmspg.dto.ApiCallLogDto;
import com.ojt_22.mmspg.entity.ApiCallLog;
import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.ApiCallLogRepository;

@ExtendWith(MockitoExtension.class)
class ApiCallLogServiceImplTest {

	@Mock
	private ApiCallLogRepository apiCallLogRepository;

	@InjectMocks
	private ApiCallLogServiceImpl service;

	@Test
	void recordLog_SavesAllFieldsOnEntity() {
		Merchant merchant = new Merchant();
		merchant.setId(UUID.randomUUID());
		ApiCredential credential = new ApiCredential();
		credential.setId(UUID.randomUUID());

		service.recordLog(merchant, credential, "/api/v1/payments", "POST", "REQ-123", "127.0.0.1",
				"{\"amount\":100}", 201, 45L, null, "a=b", "JUnit/5");

		ArgumentCaptor<ApiCallLog> captor = ArgumentCaptor.forClass(ApiCallLog.class);
		verify(apiCallLogRepository).save(captor.capture());
		ApiCallLog saved = captor.getValue();
		assertEquals(merchant, saved.getMerchant());
		assertEquals(credential, saved.getCredential());
		assertEquals("/api/v1/payments", saved.getEndpoint());
		assertEquals("POST", saved.getHttpMethod());
		assertEquals("REQ-123", saved.getRequestId());
		assertEquals("127.0.0.1", saved.getIpAddress());
		assertEquals("{\"amount\":100}", saved.getRequestBody());
		assertEquals(201, saved.getResponseStatus());
		assertEquals(45L, saved.getResponseTimeMs());
		assertEquals(null, saved.getErrorMessage());
		assertEquals("a=b", saved.getQueryParams());
		assertEquals("JUnit/5", saved.getUserAgent());
	}

	@Test
	void recordLog_WithErrorMessage_StoresError() {
		service.recordLog(null, null, "/api/v1/payments", "GET", "REQ-1", "10.0.0.1", null, 500, 12L, "boom",
				null, null);

		ArgumentCaptor<ApiCallLog> captor = ArgumentCaptor.forClass(ApiCallLog.class);
		verify(apiCallLogRepository).save(captor.capture());
		assertEquals("boom", captor.getValue().getErrorMessage());
		assertEquals(500, captor.getValue().getResponseStatus());
	}

	@Test
	void getLogsByMerchant_MapsEntitiesToDtos() {
		UUID merchantId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);
		LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 30);

		ApiCallLog log = new ApiCallLog();
		log.setId(UUID.randomUUID());
		log.setEndpoint("/api/v1/payments");
		log.setHttpMethod("POST");
		log.setRequestId("REQ-9");
		log.setIpAddress("192.168.0.1");
		log.setResponseStatus(200);
		log.setResponseTimeMs(33L);
		log.setErrorMessage("none");
		log.setCreatedAt(createdAt);

		when(apiCallLogRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable))
				.thenReturn(new PageImpl<>(List.of(log), pageable, 1));

		Page<ApiCallLogDto> result = service.getLogsByMerchant(merchantId, pageable);

		assertEquals(1, result.getTotalElements());
		ApiCallLogDto dto = result.getContent().get(0);
		assertEquals(log.getId(), dto.getLogId());
		assertEquals("/api/v1/payments", dto.getEndpoint());
		assertEquals("POST", dto.getHttpMethod());
		assertEquals("REQ-9", dto.getRequestId());
		assertEquals("192.168.0.1", dto.getIpAddress());
		assertEquals(200, dto.getResponseStatus());
		assertEquals(33L, dto.getResponseTimeMs());
		assertEquals("none", dto.getErrorMessage());
		assertEquals(createdAt, dto.getCreatedAt());
	}

	@Test
	void getLogsByMerchant_NoLogs_ReturnsEmptyPage() {
		UUID merchantId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 10);
		when(apiCallLogRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable))
				.thenReturn(Page.empty(pageable));

		Page<ApiCallLogDto> result = service.getLogsByMerchant(merchantId, pageable);

		assertTrue(result.isEmpty());
		assertEquals(0, result.getTotalElements());
	}

	@Test
	void getLogsByMerchant_PreservesPagingInfo() {
		UUID merchantId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(1, 2);
		ApiCallLog a = new ApiCallLog();
		a.setEndpoint("/a");
		ApiCallLog b = new ApiCallLog();
		b.setEndpoint("/b");
		when(apiCallLogRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, pageable))
				.thenReturn(new PageImpl<>(List.of(a, b), pageable, 6));

		Page<ApiCallLogDto> result = service.getLogsByMerchant(merchantId, pageable);

		assertEquals(2, result.getContent().size());
		assertEquals(6, result.getTotalElements());
		assertEquals(3, result.getTotalPages());
		assertEquals(1, result.getNumber());
		assertEquals("/a", result.getContent().get(0).getEndpoint());
		assertEquals("/b", result.getContent().get(1).getEndpoint());
	}
}
