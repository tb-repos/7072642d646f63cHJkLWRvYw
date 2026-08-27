package com.tourbhook.api.service;
import com.tourbhook.api.dto.payment.*;

import java.util.List;

public interface PaymentGatewayService {
    PaymentOrderResponse createOrder(CreatePaymentOrderRequest request);
    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request);
    PaymentWebhookResponse processWebhook(String signature, String payload);
    List<PaymentOrderResponse> getMyOrders();
    PaymentOrderResponse getOrder(String id);

    InvoiceMetaResponse getInvoiceMeta(String id);
    byte[] downloadInvoice(String id);
}