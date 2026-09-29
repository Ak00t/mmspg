package com.ojt_22.mmspg.controller.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.WebhookDeliveryDto;
import com.ojt_22.mmspg.service.WebhookDeliveryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/webhook-deliveries")
@RequiredArgsConstructor
public class WebhookDeliveryController {

    private final WebhookDeliveryService webhookDeliveryService;

    // Merchant ID အလိုက် ပို့ဆောင်မှုမှတ်တမ်းများ ကြည့်ရှုခြင်း[cite: 14]
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Page<WebhookDeliveryDto>> getDeliveries(
            @PathVariable UUID merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(webhookDeliveryService.getDeliveriesByMerchant(merchantId, PageRequest.of(page, size)));
    }

    // မအောင်မြင်သော Webhook ကို အတင်းအကျပ် Manual ပြန်ပို့ခြင်း (Manual Redelivery)[cite: 14]
    @PostMapping("/{deliveryId}/redeliver")
    public ResponseEntity<String> redeliver(@PathVariable UUID deliveryId) {
        webhookDeliveryService.redeliver(deliveryId);
        return ResponseEntity.ok("Redelivery task triggered successfully.");
    }
}