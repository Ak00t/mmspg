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

import com.ojt_22.mmspg.dto.CreateCredentialRequest;
import com.ojt_22.mmspg.dto.CredentialResponse;
import com.ojt_22.mmspg.service.ApiCredentialService;

@ExtendWith(MockitoExtension.class)
class ApiCredentialControllerTest {

	@Mock
	private ApiCredentialService apiCredentialService;

	@InjectMocks
	private ApiCredentialController controller;

	@Test
	void createCredential_Returns201WithBody() {
		CreateCredentialRequest request = new CreateCredentialRequest();
		request.setKeyName("my-key");
		CredentialResponse expected = CredentialResponse.builder().credentialId(UUID.randomUUID())
				.clientId("mms_test_abc").build();
		when(apiCredentialService.createApiCredential(request)).thenReturn(expected);

		ResponseEntity<CredentialResponse> result = controller.createCredential(request);

		assertEquals(HttpStatus.CREATED, result.getStatusCode());
		assertSame(expected, result.getBody());
		verify(apiCredentialService).createApiCredential(request);
	}

	@Test
	void createCredential_ServiceThrows_PropagatesException() {
		CreateCredentialRequest request = new CreateCredentialRequest();
		when(apiCredentialService.createApiCredential(request))
				.thenThrow(new IllegalArgumentException("Merchant not found"));

		assertThrows(IllegalArgumentException.class, () -> controller.createCredential(request));
	}

	@Test
	void regenerateSecret_Returns200WithBody() {
		UUID id = UUID.randomUUID();
		CredentialResponse expected = CredentialResponse.builder().credentialId(id).clientSecret("new-secret")
				.build();
		when(apiCredentialService.regenerateClientSecret(id)).thenReturn(expected);

		ResponseEntity<CredentialResponse> result = controller.regenerateSecret(id);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertSame(expected, result.getBody());
	}

	@Test
	void regenerateSecret_ServiceThrows_PropagatesException() {
		UUID id = UUID.randomUUID();
		when(apiCredentialService.regenerateClientSecret(id)).thenThrow(new IllegalArgumentException("not found"));

		assertThrows(IllegalArgumentException.class, () -> controller.regenerateSecret(id));
	}

	@Test
	void revokeCredential_Returns200WithSuccessMessage() {
		UUID id = UUID.randomUUID();

		ResponseEntity<String> result = controller.revokeCredential(id);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("API Credential has been revoked successfully.", result.getBody());
		verify(apiCredentialService).revokeApiCredential(id);
	}

	@Test
	void revokeCredential_ServiceThrows_PropagatesException() {
		UUID id = UUID.randomUUID();
		doThrow(new IllegalArgumentException("not found")).when(apiCredentialService).revokeApiCredential(id);

		assertThrows(IllegalArgumentException.class, () -> controller.revokeCredential(id));
	}
}
