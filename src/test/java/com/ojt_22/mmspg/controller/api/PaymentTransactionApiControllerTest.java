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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.corebanking.secret-key=test-secret-key-123") // @Value အတွက် ယာယီ Key
public class PaymentTransactionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PaymentTransactionService transactionService;

    @MockitoBean
    private AuthenticatedMerchantService authenticatedMerchantService;

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
}