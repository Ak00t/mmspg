package com.ojt_22.mmspg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ojt_22.mmspg.dto.CreateCredentialRequest;
import com.ojt_22.mmspg.dto.CredentialResponse;
import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.enums.ApiCredentialEnvironment;
import com.ojt_22.mmspg.enums.ApiCredentialStatus;
import com.ojt_22.mmspg.repository.ApiCredentialRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;

@ExtendWith(MockitoExtension.class)
class ApiCredentialServiceTest {

	private static final UUID DEFAULT_STAFF_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

	@Mock
	private ApiCredentialRepository credentialRepository;
	@Mock
	private MerchantRepository merchantRepository;
	@Mock
	private StaffUserRepository staffUserRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private AuthenticatedMerchantService authenticatedMerchantService;

	private ApiCredentialService service;

	private UUID merchantId;
	private Merchant merchant;

	@BeforeEach
	void setUp() {
		service = new ApiCredentialService(credentialRepository, merchantRepository, staffUserRepository,
				passwordEncoder, authenticatedMerchantService);
		merchantId = UUID.randomUUID();
		merchant = new Merchant();
		merchant.setId(merchantId);
	}

	private CreateCredentialRequest request(UUID reqMerchantId, String env) {
		CreateCredentialRequest request = new CreateCredentialRequest();
		request.setMerchantId(reqMerchantId);
		request.setEnviroment(env);
		request.setKeyName("test-key");
		request.setIpAddress("10.0.0.1");
		return request;
	}

	private void stubSuccessfulCreateDependencies() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
		when(staffUserRepository.findById(DEFAULT_STAFF_ID)).thenReturn(Optional.of(new StaffUser()));
		when(passwordEncoder.encode(anyString())).thenReturn("hashed");
		when(credentialRepository.save(any(ApiCredential.class))).thenAnswer(inv -> {
			ApiCredential c = inv.getArgument(0);
			c.setId(UUID.randomUUID());
			c.setCreatedAt(LocalDateTime.now());
			return c;
		});
	}

	private ApiCredential credentialOwnedBy(UUID ownerId) {
		Merchant owner = new Merchant();
		owner.setId(ownerId);
		ApiCredential credential = new ApiCredential();
		credential.setId(UUID.randomUUID());
		credential.setMerchant(owner);
		credential.setClientId("mms_test_abcdef1234567890");
		credential.setClientSecretHash("old-hash");
		credential.setEnvironment(ApiCredentialEnvironment.SANDBOX);
		credential.setStatus(ApiCredentialStatus.ACTIVE);
		return credential;
	}

	// ---------- generateClientId / generateClientSecret ----------

	@Test
	void generateClientId_Sandbox_UsesTestPrefix() {
		String id = service.generateClientId("SANDBOX");

		assertTrue(id.startsWith("mms_test_"));
		assertEquals("mms_test_".length() + 16, id.length());
	}

	@Test
	void generateClientId_SandboxIsCaseInsensitive() {
		assertTrue(service.generateClientId("sandbox").startsWith("mms_test_"));
	}

	@Test
	void generateClientId_Production_UsesLivePrefix() {
		String id = service.generateClientId("PRODUCTION");

		assertTrue(id.startsWith("mms_live_"));
		assertEquals("mms_live_".length() + 16, id.length());
	}

	@Test
	void generateClientId_ReturnsUniqueValues() {
		assertNotEquals(service.generateClientId("SANDBOX"), service.generateClientId("SANDBOX"));
	}

	@Test
	void generateClientSecret_IsUrlSafeBase64WithoutPadding() {
		String secret = service.generateClientSecret();

		assertEquals(43, secret.length()); // 32 bytes -> 43 base64 chars without padding
		assertTrue(secret.matches("[A-Za-z0-9_-]+"));
	}

	@Test
	void generateClientSecret_ReturnsUniqueValues() {
		assertNotEquals(service.generateClientSecret(), service.generateClientSecret());
	}

	// ---------- createApiCredential ----------

	@Test
	void createApiCredential_Sandbox_ReturnsResponseAndSavesHashedSecret() {
		stubSuccessfulCreateDependencies();

		CredentialResponse response = service.createApiCredential(request(merchantId, "sandbox"));

		assertNotNull(response.getCredentialId());
		assertTrue(response.getClientId().startsWith("mms_test_"));
		assertEquals("SANDBOX", response.getEnvironment());
		assertEquals("ACTIVE", response.getStatus());
		assertEquals("10.0.0.1", response.getIpAddress());
		assertNotNull(response.getClientSecret());
		assertNotNull(response.getExpiresAt());

		ArgumentCaptor<ApiCredential> captor = ArgumentCaptor.forClass(ApiCredential.class);
		verify(credentialRepository).save(captor.capture());
		ApiCredential saved = captor.getValue();
		assertEquals("hashed", saved.getClientSecretHash());
		assertNotEquals(response.getClientSecret(), saved.getClientSecretHash());
		assertEquals(merchant, saved.getMerchant());
		assertEquals("test-key", saved.getKeyName());
		assertEquals(100, saved.getRateLimit());
		assertEquals(ApiCredentialStatus.ACTIVE, saved.getStatus());
		assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now().plusMonths(11)));
		verify(passwordEncoder).encode(response.getClientSecret());
	}

	@Test
	void createApiCredential_Production_UsesLivePrefix() {
		stubSuccessfulCreateDependencies();

		CredentialResponse response = service.createApiCredential(request(merchantId, "production"));

		assertTrue(response.getClientId().startsWith("mms_live_"));
		assertEquals("PRODUCTION", response.getEnvironment());
	}

	@Test
	void createApiCredential_NullRequestMerchantId_UsesAuthenticatedMerchant() {
		stubSuccessfulCreateDependencies();

		CredentialResponse response = service.createApiCredential(request(null, "sandbox"));

		assertNotNull(response);
		verify(merchantRepository).findById(merchantId);
	}

	@Test
	void createApiCredential_MerchantIdMismatch_ThrowsIllegalArgument() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.createApiCredential(request(UUID.randomUUID(), "sandbox")));

		assertEquals("Merchant ID does not match authenticated merchant", ex.getMessage());
		verify(credentialRepository, never()).save(any());
	}

	@Test
	void createApiCredential_MerchantNotFound_ThrowsIllegalArgument() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.createApiCredential(request(merchantId, "sandbox")));

		assertTrue(ex.getMessage().startsWith("Merchant not found"));
		verify(credentialRepository, never()).save(any());
	}

	@Test
	void createApiCredential_DefaultStaffMissing_FallsBackToFirstStaff() {
		StaffUser fallback = new StaffUser();
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
		when(staffUserRepository.findById(DEFAULT_STAFF_ID)).thenReturn(Optional.empty());
		when(staffUserRepository.findAll()).thenReturn(List.of(fallback));
		when(passwordEncoder.encode(anyString())).thenReturn("hashed");
		when(credentialRepository.save(any(ApiCredential.class))).thenAnswer(inv -> inv.getArgument(0));

		service.createApiCredential(request(merchantId, "sandbox"));

		ArgumentCaptor<ApiCredential> captor = ArgumentCaptor.forClass(ApiCredential.class);
		verify(credentialRepository).save(captor.capture());
		assertEquals(fallback, captor.getValue().getCreatedBy());
	}

	@Test
	void createApiCredential_NoStaffAtAll_ThrowsIllegalState() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
		when(staffUserRepository.findById(DEFAULT_STAFF_ID)).thenReturn(Optional.empty());
		when(staffUserRepository.findAll()).thenReturn(List.of());

		assertThrows(IllegalStateException.class,
				() -> service.createApiCredential(request(merchantId, "sandbox")));

		verify(credentialRepository, never()).save(any());
	}

	@Test
	void createApiCredential_InvalidEnvironment_ThrowsIllegalArgument() {
		when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
		when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
		when(staffUserRepository.findById(DEFAULT_STAFF_ID)).thenReturn(Optional.of(new StaffUser()));
		when(passwordEncoder.encode(anyString())).thenReturn("hashed");

		assertThrows(IllegalArgumentException.class,
				() -> service.createApiCredential(request(merchantId, "staging")));

		verify(credentialRepository, never()).save(any());
	}

	// ---------- regenerateClientSecret ----------

	@Test
	void regenerateClientSecret_ReplacesHashAndReturnsNewRawSecret() {
		ApiCredential credential = credentialOwnedBy(merchantId);
		credential.setIpAddress("1.2.3.4");
		when(credentialRepository.findById(credential.getId())).thenReturn(Optional.of(credential));
		when(passwordEncoder.encode(anyString())).thenReturn("new-hash");

		CredentialResponse response = service.regenerateClientSecret(credential.getId());

		assertEquals("new-hash", credential.getClientSecretHash());
		assertNotNull(response.getClientSecret());
		assertEquals(credential.getId(), response.getCredentialId());
		assertEquals(credential.getClientId(), response.getClientId());
		assertEquals("SANDBOX", response.getEnvironment());
		assertEquals("ACTIVE", response.getStatus());
		assertEquals("1.2.3.4", response.getIpAddress());
		verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
		verify(passwordEncoder).encode(response.getClientSecret());
		verify(credentialRepository).save(credential);
	}

	@Test
	void regenerateClientSecret_NotFound_ThrowsIllegalArgument() {
		UUID id = UUID.randomUUID();
		when(credentialRepository.findById(id)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> service.regenerateClientSecret(id));

		assertTrue(ex.getMessage().contains(id.toString()));
		verify(credentialRepository, never()).save(any());
	}

	@Test
	void regenerateClientSecret_AccessDenied_DoesNotChangeSecret() {
		ApiCredential credential = credentialOwnedBy(UUID.randomUUID());
		when(credentialRepository.findById(credential.getId())).thenReturn(Optional.of(credential));
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(credential.getMerchant().getId());

		assertThrows(AccessDeniedException.class, () -> service.regenerateClientSecret(credential.getId()));

		assertEquals("old-hash", credential.getClientSecretHash());
		verify(credentialRepository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	// ---------- revokeApiCredential ----------

	@Test
	void revokeApiCredential_SetsStatusRevokedAndTimestamp() {
		ApiCredential credential = credentialOwnedBy(merchantId);
		assertNull(credential.getRevokedAt());
		when(credentialRepository.findById(credential.getId())).thenReturn(Optional.of(credential));

		service.revokeApiCredential(credential.getId());

		assertEquals(ApiCredentialStatus.REVOKED, credential.getStatus());
		assertNotNull(credential.getRevokedAt());
		verify(authenticatedMerchantService).assertMerchantAccess(merchantId);
		verify(credentialRepository).save(credential);
	}

	@Test
	void revokeApiCredential_NotFound_ThrowsIllegalArgument() {
		UUID id = UUID.randomUUID();
		when(credentialRepository.findById(eq(id))).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.revokeApiCredential(id));

		verify(credentialRepository, never()).save(any());
	}

	@Test
	void revokeApiCredential_AccessDenied_KeepsCredentialActive() {
		ApiCredential credential = credentialOwnedBy(UUID.randomUUID());
		when(credentialRepository.findById(credential.getId())).thenReturn(Optional.of(credential));
		doThrow(new AccessDeniedException("denied")).when(authenticatedMerchantService)
				.assertMerchantAccess(credential.getMerchant().getId());

		assertThrows(AccessDeniedException.class, () -> service.revokeApiCredential(credential.getId()));

		assertEquals(ApiCredentialStatus.ACTIVE, credential.getStatus());
		assertFalse(credential.getRevokedAt() != null);
		verify(credentialRepository, never()).save(any());
	}
}
