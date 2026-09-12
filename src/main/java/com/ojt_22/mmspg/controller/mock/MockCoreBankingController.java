package com.ojt_22.mmspg.controller.mock;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.CoreBankingDebitRequestDto;
import com.ojt_22.mmspg.dto.CoreBankingResponseDto;

@RestController
@RequestMapping("/api/v1/core")
public class MockCoreBankingController {

    @PostMapping("/debit")
    public ResponseEntity<CoreBankingResponseDto> mockDebit(@RequestBody CoreBankingDebitRequestDto request) {
        
        // 💡 1. Failure Scenario စမ်းသပ်ရန်
        // Customer ID က "FAIL_CUST" ဖြစ်နေလျှင် သို့မဟုတ် Amount က 1,000,000 ထက် ကျော်လွန်နေလျှင် Fail အဖြစ် ပြန်မည်
        if ("FAIL_CUST".equals(request.getCustomerId()) || request.getAmount().doubleValue() > 1000000) {
            return ResponseEntity.ok(CoreBankingResponseDto.builder()
                    .success(false)
                    .coreTransactionRef(null)
                    .failureReason("Insufficient account balance or account locked")
                    .build());
        }

        // 💡 2. Success Scenario စမ်းသပ်ရန်
        return ResponseEntity.ok(CoreBankingResponseDto.builder()
                .success(true)
                .coreTransactionRef("CORE-TXN-" + System.currentTimeMillis())
                .failureReason(null)
                .build());
    }
}