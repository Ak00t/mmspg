package com.ojt_22.mmspg.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.ojt_22.mmspg.entity.ApiCredential;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.ApiCredentialRepository;
import com.ojt_22.mmspg.service.ApiCallLogService;
import com.ojt_22.mmspg.utils.ApiLogUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component // Spring Boot မှ အလိုအလျောက် သိရှိပြီး Filter အဖြစ် အလုပ်လုပ်ပေးမည်
@RequiredArgsConstructor
public class ApiLoggingFilter extends OncePerRequestFilter {

    private final ApiCallLogService apiCallLogService;
    private final ApiCredentialRepository apiCredentialRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ၁။ API လမ်းကြောင်းများ (/api/v1/...) ကိုသာ Log မှတ်မည်။ (ပုံများ၊ CSS များအတွက် မမှတ်ပါ)
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ၂။ Request နှင့် Response Body ကို အကြိမ်ကြိမ် ဖတ်နိုင်ရန် Wrapper ဖြင့် ထုပ်ပိုးခြင်း
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 0);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // ၃။ Filter အလုပ်လုပ်ပြီးနောက် သက်ဆိုင်ရာ Controller ဆီသို့ API ကို ဆက်လက် အလုပ်လုပ်စေခြင်း
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // ၄။ Controller အလုပ်လုပ်ပြီးချိန် (သို့မဟုတ် Error တက်ပြီးချိန်) တွင် ကြာချိန်ကို တွက်ချက်ခြင်း
            long duration = System.currentTimeMillis() - startTime;

            // ၅။ ဝင်လာသည့် JSON Body ကို ဖတ်ပြီး Customer PIN များကို Mask (******) လုပ်ခြင်း
            String requestBody = getRequestBody(wrappedRequest);
            String maskedBody = ApiLogUtils.maskSensitiveData(requestBody);

            // ၆။ Group 5 ပို့လိုက်သည့် Header ထဲမှ X-Client-ID ကို ရှာဖွေခြင်း
            String clientId = wrappedRequest.getHeader("X-Client-ID");
            ApiCredential credential = null;
            Merchant merchant = null;

            // ၇။ Client ID ပါလာပါက Database တွင် သွားရှာပြီး သက်ဆိုင်ရာ Merchant ကို ဆွဲထုတ်ခြင်း
            if (clientId != null && !clientId.isEmpty()) {
                credential = apiCredentialRepository.findByClientIdAndStatus(clientId, com.ojt_22.mmspg.enums.ApiCredentialStatus.ACTIVE).orElse(null);
                if (credential != null) {
                    merchant = credential.getMerchant();
                }
            }

            // ၈။ HTTP Status 400 အထက် ဖြစ်ပါက Error Message မှတ်သားခြင်း
            String errorMessage = null;
            if (wrappedResponse.getStatus() >= 400) {
                errorMessage = "API execution failed with HTTP Status: " + wrappedResponse.getStatus();
            }

            // ၉။ ApiCallLogService မှတစ်ဆင့် Database (api_call_logs) ထဲသို့ အလိုအလျောက် သိမ်းဆည်းခြင်း
            apiCallLogService.recordLog(
                    merchant,
                    credential,
                    wrappedRequest.getRequestURI(),
                    wrappedRequest.getMethod(),
                    ApiLogUtils.getOrCreateRequestId(wrappedRequest),
                    ApiLogUtils.getClientIp(wrappedRequest),
                    maskedBody,
                    wrappedResponse.getStatus(),
                    duration,
                    errorMessage,
                    wrappedRequest.getQueryString(),
                    wrappedRequest.getHeader("User-Agent")
            );

            // ၁၀။ Group 5 သို့ပေးမည့် Response Body ကို အမှန်တကယ် ပြန်လည်ပေးပို့ခြင်း (မပါလျှင် Group 5 ဘက်တွင် အလွတ်ကြီး ထွက်နေမည်)
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * ContentCachingRequestWrapper ထဲမှ Request Body (JSON) ကို String အဖြစ်သို့ ပြောင်းပေးသည့် Method
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, buf.length, request.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unsupported Encoding]";
            }
        }
        return null;
    }
}