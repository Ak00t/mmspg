package com.ojt_22.mmspg.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ojt_22.mmspg.dto.WebhookDeliveryDto;
import com.ojt_22.mmspg.entity.PaymentTransaction;

public interface WebhookDeliveryService {
    
    
    void sendWebhook(PaymentTransaction transaction, String eventType);
    
    
    void executeDelivery(UUID deliveryId);
    
   
    Page<WebhookDeliveryDto> getDeliveriesByMerchant(UUID merchantId, Pageable pageable);
    
   
    void redeliver(UUID deliveryId);
}