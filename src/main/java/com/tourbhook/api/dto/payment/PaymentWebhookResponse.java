package com.tourbhook.api.dto.payment;

public record PaymentWebhookResponse(
        boolean processed,
        String event,
        String orderId,
        String paymentId,
        String status
) {
}
