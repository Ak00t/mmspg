package com.ojt_22.mmspg.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ojt_22.mmspg.entity.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    // 1. Transaction Reference ဖြင့် ရှာရန် (Status API အတွက်)
    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);

    // 2. Payment Token ဖြင့် ရှာရန် (Authorize API အတွက်)
    Optional<PaymentTransaction> findByPaymentToken(String paymentToken);

    // 3. Merchant ID အလိုက် Transaction များ စာရင်းထုတ်ရန် (Transaction API အတွက်)
    List<PaymentTransaction> findByMerchantId(UUID merchantId);
}