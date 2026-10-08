package com.ojt_22.mmspg.controller.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ojt_22.mmspg.dto.CreateWebhookRequest;
import com.ojt_22.mmspg.annotation.Auditable;
import com.ojt_22.mmspg.dto.UpdateWebhookRequest;
import com.ojt_22.mmspg.dto.WebhookConfigResponse;
import com.ojt_22.mmspg.service.WebhookConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {


    private final WebhookConfigService webhookConfigService;

    @PostMapping
    @Auditable(menuName = "Webhook Configuration", action = "CREATE", targetType = "WEBHOOK_CONFIG")
    public ResponseEntity<WebhookConfigResponse> createWebhook(@RequestBody CreateWebhookRequest request) {
        WebhookConfigResponse response = webhookConfigService.createWebhook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{webhookId}")
    @Auditable(menuName = "Webhook Configuration", action = "UPDATE", targetType = "WEBHOOK_CONFIG")
    public ResponseEntity<WebhookConfigResponse> updateWebhook(
            @PathVariable UUID webhookId,
            @RequestBody UpdateWebhookRequest request
    ) {
        WebhookConfigResponse response = webhookConfigService.updateWebhook(webhookId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{webhookId}")
    @Auditable(menuName = "Webhook Configuration", action = "DELETE", targetType = "WEBHOOK_CONFIG")
    public ResponseEntity<String> deleteWebhook(@PathVariable UUID webhookId) {
        webhookConfigService.deleteWebhook(webhookId);
        return ResponseEntity.ok("Webhook configuration has been deactivated successfully.");
    }

    @PostMapping("/{webhookId}/test")
    @Auditable(menuName = "Webhook Configuration", action = "TEST", targetType = "WEBHOOK_CONFIG")
    public ResponseEntity<String> testWebhook(@PathVariable UUID webhookId) {
        String result = webhookConfigService.testWebhook(webhookId);
        return ResponseEntity.ok(result);
    }
}
