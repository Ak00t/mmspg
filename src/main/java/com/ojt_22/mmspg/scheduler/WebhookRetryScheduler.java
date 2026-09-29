package com.ojt_22.mmspg.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ojt_22.mmspg.entity.WebhookDelivery;
import com.ojt_22.mmspg.enums.WebhookDeliveryStatus;
import com.ojt_22.mmspg.repository.WebhookDeliveryRepository;
import com.ojt_22.mmspg.service.WebhookDeliveryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRetryScheduler {

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookDeliveryService webhookDeliveryService;

    // စက္ကန့် ၃၀ တိုင်း တစ်ကြိမ် အလိုအလျောက် စစ်ဆေးပြီး Retry ပြုလုပ်ခြင်း[cite: 14]
    @Scheduled(fixedDelay = 30000)
    public void processPendingDeliveries() {
        List<WebhookDelivery> pendingDeliveries = webhookDeliveryRepository
                .findByStatusAndNextRetryAtBefore(WebhookDeliveryStatus.PENDING, LocalDateTime.now());

        if (!pendingDeliveries.isEmpty()) {
            log.info("Found {} pending webhook deliveries to retry", pendingDeliveries.size());
            for (WebhookDelivery delivery : pendingDeliveries) {
                try {
                    webhookDeliveryService.executeDelivery(delivery.getId());
                } catch (Exception e) {
                    log.error("Failed to process retry for delivery ID: {}", delivery.getId(), e);
                }
            }
        }
    }
}