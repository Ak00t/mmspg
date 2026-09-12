package com.ojt_22.mmspg.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.ojt_22.mmspg.dto.CoreBankingDebitRequestDto;
import com.ojt_22.mmspg.dto.CoreBankingResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoreBankingClient {

    private final RestTemplate restTemplate;

    // Group 2 Core Banking ရဲ့ API Endpoint URL (application.properties တွင် ပြင်ဆင်နိုင်သည်)
    @Value("${core.banking.service.url:http://localhost:8004/api/v1/core/debit}")
    private String coreBankingDebitUrl;

public CoreBankingResponseDto executeDebit(String customerId, BigDecimal amount, BigDecimal feeAmount, String transactionReference) {
        
        // 2. Request Body ပြင်ဆင်သည့်နေရာတွင် .feeAmount(feeAmount) ထည့်လိုက်ပါသည်
        CoreBankingDebitRequestDto request = CoreBankingDebitRequestDto.builder()
                .customerId(customerId)
                .amount(amount)
                .feeAmount(feeAmount) // <-- Fee Amount ပေါင်းထည့်ထားပါသည်
                .transactionReference(transactionReference)
                .build();

        try {
            // 2. Group 2 Core Banking API ဆီသို့ POST Request လှမ်းခေါ်ခြင်း
            CoreBankingResponseDto response = restTemplate.postForObject(
                    coreBankingDebitUrl,
                    request,
                    CoreBankingResponseDto.class
            );

            if (response != null) {
                return response;
            }

            return CoreBankingResponseDto.builder()
                    .success(false)
                    .failureReason("Received empty response from Core Banking")
                    .build();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Core Banking Debit Failed. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return CoreBankingResponseDto.builder()
                    .success(false)
                    .failureReason("Core Banking Authorization Failed: " + e.getResponseBodyAsString())
                    .build();
        } catch (Exception e) {
            log.error("Failed to connect to Core Banking service", e);
            return CoreBankingResponseDto.builder()
                    .success(false)
                    .failureReason("Connection error to Core Banking service: " + e.getMessage())
                    .build();
        }
    }
}