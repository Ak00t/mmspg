package com.ojt_22.mmspg.utils;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

public final class ApiLogUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "X-Real-IP"
    };

    private ApiLogUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

        public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                
                return ipList.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    
    public static String getOrCreateRequestId(HttpServletRequest request) {
        if (request != null) {
            String requestId = request.getHeader("X-Request-ID");
            if (requestId != null && !requestId.trim().isEmpty()) {
                return requestId.trim();
            }
        }
        return "REQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

   
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "... [TRUNCATED]";
    }

    
    public static String maskSensitiveData(String jsonBody) {
        if (jsonBody == null || jsonBody.isEmpty()) {
            return jsonBody;
        }
      
        return jsonBody.replaceAll("(?i)\"(pin|password|clientSecret|secretKey)\"\\s*:\\s*\"([^\"]+)\"", "\"$1\":\"******\"");
    }
}