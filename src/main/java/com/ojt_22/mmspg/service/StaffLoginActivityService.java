package com.ojt_22.mmspg.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.StaffLoginActivityResponseDto;
import com.ojt_22.mmspg.entity.StaffLoginActivity;
import com.ojt_22.mmspg.repository.StaffLoginActivityRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffLoginActivityService {

    private final StaffLoginActivityRepository loginActivityRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public StaffLoginActivity recordLoginActivity(UUID staffId, HttpServletRequest request, String status) {
        String userAgent = request != null ? request.getHeader("User-Agent") : "";
        String ipAddress = extractClientIp(request);
        String device = parseDevice(userAgent);
        String browser = parseBrowser(userAgent);
        String location = resolveLocation(ipAddress);
        String finalStatus = status != null ? status : "SUCCESS";

        java.util.List<StaffLoginActivity> existingActivities = loginActivityRepository
                .findByStaffIdAndDeviceAndBrowser(staffId, device, browser);

        StaffLoginActivity activity;
        if (!existingActivities.isEmpty()) {
            // ရှိပြီးသားထဲမှ ပထမဆုံး එකကို ယူ၍ အချိန် Update လုပ်ခြင်း
            activity = existingActivities.get(0);
            activity.setLoginTime(LocalDateTime.now());
            activity.setIpAddress(ipAddress);
            activity.setStatus(finalStatus);
            
            // အကယ်၍ အခြားထပ်နေသည်များ ရှိပါက ရှင်းလင်းလိုလျှင် ဖျက်ပစ်နိုင်သည်
        } else {
            // မရှိသေးမှသာ အသစ်ဆောက်ခြင်း
            activity = StaffLoginActivity.builder()
                    .staffId(staffId)
                    .ipAddress(ipAddress)
                    .device(device)
                    .browser(browser)
                    .location(location)
                    .status(finalStatus)
                    .loginTime(LocalDateTime.now())
                    .build();
        }

        return loginActivityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<StaffLoginActivityResponseDto> getRecentActivities(UUID staffId) {
        return loginActivityRepository.findTop4ByStaffIdOrderByLoginTimeDesc(staffId)
                .stream()
                .map(activity -> StaffLoginActivityResponseDto.builder()
                        .id(activity.getId())
                        .timestamp(activity.getLoginTime() != null ? activity.getLoginTime().format(FORMATTER) : "")
                        .ipAddress(activity.getIpAddress())
                        .device(activity.getDevice())
                        .browser(activity.getBrowser())
                        .location(activity.getLocation())
                        .status(activity.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String[] headers = {
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
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private String parseDevice(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Windows PC";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone")) {
            return "iPhone";
        } else if (ua.contains("ipad")) {
            return "iPad";
        } else if (ua.contains("android")) {
            return "Android Mobile";
        } else if (ua.contains("windows")) {
            return "Windows PC";
        } else if (ua.contains("macintosh") || ua.contains("mac os")) {
            return "Mac";
        } else if (ua.contains("linux")) {
            return "Linux PC";
        }
        return "Desktop";
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Chrome";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/") || ua.contains("edge/")) {
            return "Edge";
        } else if (ua.contains("opr/") || ua.contains("opera/")) {
            return "Opera";
        } else if (ua.contains("chrome/") && !ua.contains("chromium/")) {
            return "Chrome";
        } else if (ua.contains("firefox/")) {
            return "Firefox";
        } else if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }
        return "Chrome";
    }

    private String resolveLocation(String ip) {
        if (ip == null || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") 
                || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            return "Yangon, Myanmar";
        }
        return "Yangon, Myanmar";
    }
}
