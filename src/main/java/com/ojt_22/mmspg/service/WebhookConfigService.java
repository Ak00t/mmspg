package com.ojt_22.mmspg.service;

import java.util.UUID;
import com.ojt_22.mmspg.dto.CreateWebhookRequest;
import com.ojt_22.mmspg.dto.UpdateWebhookRequest;
import com.ojt_22.mmspg.dto.WebhookConfigResponse;

public interface WebhookConfigService {

    /**
     * Webhook အသစ် ဖန်တီးခြင်း
     */
    WebhookConfigResponse createWebhook(CreateWebhookRequest request);

    /**
     * Webhook အချက်အလက် ပြင်ဆင်ခြင်း
     */
    WebhookConfigResponse updateWebhook(UUID webhookId, UpdateWebhookRequest request);

    /**
     * Webhook ပိတ်သိမ်းခြင်း (Deactivate/Soft Delete)
     */
    void deleteWebhook(UUID webhookId);

    /**
     * Webhook Callback URL ကို စမ်းသပ် Ping ပို့ခြင်း
     */
    String testWebhook(UUID webhookId);
}