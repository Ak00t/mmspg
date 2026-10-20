package com.ojt_22.mmspg.controller.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ojt_22.mmspg.config.SecurityConfig;
import com.ojt_22.mmspg.filter.ApiLoggingFilter;
import com.ojt_22.mmspg.repository.MerchantLedgerRepository;
import com.ojt_22.mmspg.security.ApiKeyAuthenticationFilter;
import com.ojt_22.mmspg.security.JwtAuthenticationFilter;

@WebMvcTest(value = MerchantLedgerApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, ApiLoggingFilter.class,
                        ApiKeyAuthenticationFilter.class, JwtAuthenticationFilter.class}))
@AutoConfigureMockMvc(addFilters = false)
public class MerchantLedgerApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerchantLedgerRepository ledgerRepository;

    @Test
    public void testGetByBalanceType_Success() throws Exception {
        when(ledgerRepository.findByBalanceType("PENDING"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/merchant-ledgers/by-balance-type")
                .param("balanceType", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateBalanceType_Success() throws Exception {
        UUID ledgerId = UUID.randomUUID();
        when(ledgerRepository.updateBalanceType(eq(ledgerId), eq("SETTLED")))
                .thenReturn(1);

        mockMvc.perform(put("/api/v1/merchant-ledgers/{ledgerId}/balance-type", ledgerId)
                .param("status", "SETTLED"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ledger balance type updated successfully to SETTLED"));
    }
}