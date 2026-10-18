package com.ojt_22.mmspg.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.enums.ApiCredentialStatus;
import com.ojt_22.mmspg.repository.ApiCredentialRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private ApiCredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthenticationFilter apiKeyFilter;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        SecurityContextHolder.clearContext();
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_NoHeaders_PassesThroughChain() throws ServletException, IOException {
        when(request.getHeader("X-Client-ID")).thenReturn(null);

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_MissingSecretHeader_Returns401() throws ServletException, IOException {
        when(request.getHeader("X-Client-ID")).thenReturn("mms_live_test123");
        when(request.getHeader("X-Client-Secret")).thenReturn(null);

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Missing X-Client-Secret header"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_InvalidSecret_Returns401() throws ServletException, IOException {
        when(request.getHeader("X-Client-ID")).thenReturn("mms_live_test123");
        when(request.getHeader("X-Client-Secret")).thenReturn("wrong-secret");

        ApiCredential credential = new ApiCredential();
        credential.setClientSecretHash("hashed-secret");
        when(credentialRepository.findByClientIdAndStatus("mms_live_test123", ApiCredentialStatus.ACTIVE))
                .thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong-secret", "hashed-secret")).thenReturn(false);

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Invalid API Client ID or Secret"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_ExpiredCredential_Returns401() throws ServletException, IOException {
        when(request.getHeader("X-Client-ID")).thenReturn("mms_live_test123");
        when(request.getHeader("X-Client-Secret")).thenReturn("valid-secret");

        ApiCredential credential = new ApiCredential();
        credential.setClientSecretHash("hashed-secret");
        credential.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday

        when(credentialRepository.findByClientIdAndStatus("mms_live_test123", ApiCredentialStatus.ACTIVE))
                .thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("valid-secret", "hashed-secret")).thenReturn(true);

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("API credential has expired"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_ValidCredentials_AuthenticatesSuccessfully() throws ServletException, IOException {
        when(request.getHeader("X-Client-ID")).thenReturn("mms_live_test123");
        when(request.getHeader("X-Client-Secret")).thenReturn("valid-secret");

        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant();
        merchant.setId(merchantId);

        ApiCredential credential = new ApiCredential();
        credential.setClientSecretHash("hashed-secret");
        credential.setMerchant(merchant);
        credential.setExpiresAt(LocalDateTime.now().plusDays(30));
        credential.setIpAddress("127.0.0.1");

        when(credentialRepository.findByClientIdAndStatus("mms_live_test123", ApiCredentialStatus.ACTIVE))
                .thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("valid-secret", "hashed-secret")).thenReturn(true);

        apiKeyFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(merchantId.toString(), SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_API_CLIENT")));
    }
}
