package com.shrinetours.api.dto.payment;

import java.time.Instant;

public record VerifyPaymentResponse(
        String orderId,
        String paymentId,
        String status,
        String subscriptionPlan,
        Instant paidAt
) {
}
