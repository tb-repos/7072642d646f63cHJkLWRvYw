package com.shrinetours.api.dto.payment;

public record PaymentWebhookResponse(
        boolean processed,
        String event,
        String orderId,
        String paymentId,
        String status
) {
}
