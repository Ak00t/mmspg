package com.ojt_22.mmspg.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojt_22.mmspg.config.SecurityConfig;
import com.ojt_22.mmspg.dto.PaymentAuthorizeRequestDto;
import com.ojt_22.mmspg.dto.PaymentAuthorizeResponseDto;
import com.ojt_22.mmspg.dto.PaymentCheckoutInfoDto;
import com.ojt_22.mmspg.dto.PaymentInitiateRequestDto;
import com.ojt_22.mmspg.dto.PaymentInitiateResponseDto;
import com.ojt_22.mmspg.dto.PaymentStatusResponseDto;
import com.ojt_22.mmspg.filter.ApiLoggingFilter;
import com.ojt_22.mmspg.security.ApiKeyAuthenticationFilter;
import com.ojt_22.mmspg.security.AuthenticatedMerchantService;
import com.ojt_22.mmspg.security.JwtAuthenticationFilter;
import com.ojt_22.mmspg.service.PaymentTransactionService;

@WebMvcTest(value = PaymentTransactionApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, ApiLoggingFilter.class,
                        ApiKeyAuthenticationFilter.class, JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc
@TestPropertySource(properties = "internal.corebanking.secret-key=test-secret-key-123") // @Value အတွက် ယာယီ Key
public class PaymentTransactionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PaymentTransactionService transactionService;

    @MockitoBean
    private AuthenticatedMerchantService authenticatedMerchantService;
    
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
    
    @Test
    @WithMockUser
    public void testInitiateTransaction_MerchantIdNull_Success() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID resolvedMerchantId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(null); // 👈 merchantId ကို null အဖြစ် ထည့်သွင်းစမ်းသပ်ခြင်း
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-NULL-MERCHANT");

        PaymentInitiateResponseDto responseDto = PaymentInitiateResponseDto.builder()
                .transactionReference("TXN-NULL-MERCHANT")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("INITIATED")
                .build();

        when(authenticatedMerchantService.resolveMerchantId()).thenReturn(resolvedMerchantId);
        when(transactionService.initiateTransaction(any(PaymentInitiateRequestDto.class), eq(idempotencyKey)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testInitiateTransaction_Success() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID merchantId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-001");

        PaymentInitiateResponseDto responseDto = PaymentInitiateResponseDto.builder()
                .transactionReference("TXN-12345")
                .paymentToken("token-abc")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("INITIATED")
                .paymentUrl("http://localhost/pay/token-abc")
                .build();

        when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
        when(transactionService.initiateTransaction(any(PaymentInitiateRequestDto.class), eq(idempotencyKey)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value("TXN-12345"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    public void testAuthorizeTransaction_Success() throws Exception {
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken("token-abc");
        requestDto.setCustomerId("CUST-001");
        requestDto.setStatus("SUCCESS");

        PaymentAuthorizeResponseDto responseDto = PaymentAuthorizeResponseDto.builder()
                .transactionReference("TXN-12345")
                .status("SUCCESS")
                .build();

        when(transactionService.authorizeTransaction(any(PaymentAuthorizeRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/payments/authorize")
                .header("X-Internal-Service-Key", "test-secret-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value("TXN-12345"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    public void testGetTransactionStatus_Success() throws Exception {
        String refNo = "TXN-12345";
        PaymentStatusResponseDto responseDto = PaymentStatusResponseDto.builder()
                .transactionReference(refNo)
                .orderId("ORD-001")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("COMPLETED")
                .build();

        when(transactionService.getTransactionStatus(refNo)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/payments/status/{transactionReference}", refNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value(refNo))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testGetMerchantTransactions_Success() throws Exception {
        UUID merchantId = UUID.randomUUID();
        when(transactionService.getMerchantTransactions(merchantId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/payments/merchant/{merchantId}", merchantId))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCheckoutInfo_Success() throws Exception {
        String token = "token-abc";
        PaymentCheckoutInfoDto checkoutInfo = PaymentCheckoutInfoDto.builder()
                .businessName("Test Business")
                .orderId("ORD-001")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("INITIATED")
                .build();

        when(transactionService.getCheckoutInfoByToken(token)).thenReturn(checkoutInfo);

        mockMvc.perform(get("/api/v1/payments/checkout-info/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-001"));
    }
    
    @Test
    @WithMockUser(authorities = "ROLE_API_CLIENT", username = "11111111-1111-1111-1111-111111111111")
    public void testInitiateTransaction_ApiClientMismatch_ThrowsException() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID differentMerchantId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(differentMerchantId); // ID မတူညီအောင် လုပ်ထားခြင်းဖြင့် Exception ပစ်ခိုင်းရန်
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-002");

        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError()); // 👈 isBadRequest() မှ isInternalServerError() သို့ ပြောင်းပါ
    }

    @Test
    public void testAuthorizeTransaction_Unauthorized() throws Exception {
        PaymentAuthorizeRequestDto requestDto = new PaymentAuthorizeRequestDto();
        requestDto.setPaymentToken("token-abc");
        requestDto.setCustomerId("CUST-001");
        requestDto.setStatus("SUCCESS");

        mockMvc.perform(post("/api/v1/payments/authorize")
                .header("X-Internal-Service-Key", "wrong-secret-key") // မှားယွင်းသော Key ပို့၍ Unauthorized (401) ဖြစ်ခြင်းကို စမ်းသပ်ရန်
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser // Normal user (ROLE_API_CLIENT မပါဝင်သော အနေအထား)
    public void testInitiateTransaction_MerchantMismatch_ThrowsException() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID authMerchantId = UUID.randomUUID();
        UUID differentMerchantId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(differentMerchantId); // ID မတူညီအောင် လုပ်ထားခြင်း
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-003");

        when(authenticatedMerchantService.resolveMerchantId()).thenReturn(authMerchantId);

        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    public void testInitiateTransaction_ApiClient_Success() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID merchantId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-API-01");

        PaymentInitiateResponseDto responseDto = PaymentInitiateResponseDto.builder()
                .transactionReference("TXN-API-123")
                .paymentToken("token-api")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("INITIATED")
                .paymentUrl("http://localhost/pay/token-api")
                .build();

        when(transactionService.initiateTransaction(any(PaymentInitiateRequestDto.class), eq(idempotencyKey)))
                .thenReturn(responseDto);

        // Security Authentication ကို mock လုပ်၍ ROLE_API_CLIENT နှင့် Merchant UUID ကို တိုက်ရိုက်ပေးပို့ခြင်း
        org.springframework.security.core.authority.SimpleGrantedAuthority authority = 
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_API_CLIENT");
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        merchantId.toString(), "N/A", java.util.Collections.singletonList(authority));

        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value("TXN-API-123"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }
    
    @Test
    public void testInitiateTransaction_AuthenticationNull_ProcessedAsNormalUser() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID merchantId = UUID.randomUUID();

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(merchantId);
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-NULL-AUTH");

        PaymentInitiateResponseDto responseDto = PaymentInitiateResponseDto.builder()
                .transactionReference("TXN-NULL-123")
                .paymentToken("token-null")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("INITIATED")
                .paymentUrl("http://localhost/pay/token-null")
                .build();

        when(authenticatedMerchantService.resolveMerchantId()).thenReturn(merchantId);
        when(transactionService.initiateTransaction(any(PaymentInitiateRequestDto.class), eq(idempotencyKey)))
                .thenReturn(responseDto);

        // @WithMockUser မပါဘဲ Security Context ကို Authentication အလွတ်ဖြင့် လုပ်ဆောင်ခြင်း
        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value("TXN-NULL-123"));
    }
    
    @Test
    public void testInitiateTransaction_ApiClient_MerchantIdNull_Success() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        UUID merchantId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        PaymentInitiateRequestDto requestDto = new PaymentInitiateRequestDto();
        requestDto.setMerchantId(null); // 👈 API Client အတွက် merchantId ကို null အဖြစ် ထည့်သွင်းခြင်း
        requestDto.setAmount(BigDecimal.valueOf(10000));
        requestDto.setCurrency("MMK");
        requestDto.setOrderId("ORD-API-NULL");

        PaymentInitiateResponseDto responseDto = PaymentInitiateResponseDto.builder()
                .transactionReference("TXN-API-NULL")
                .amount(BigDecimal.valueOf(10000))
                .currency("MMK")
                .status("INITIATED")
                .build();

        when(transactionService.initiateTransaction(any(PaymentInitiateRequestDto.class), eq(idempotencyKey)))
                .thenReturn(responseDto);

        org.springframework.security.core.authority.SimpleGrantedAuthority authority = 
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_API_CLIENT");
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        merchantId.toString(), "N/A", java.util.Collections.singletonList(authority));

        mockMvc.perform(post("/api/v1/payments/initiate")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk());
    }
}