package com.shrinetours.api.dto.payment;

import java.time.Instant;
import java.time.LocalDateTime;

public record PaymentOrderResponse(
        String id,
        String planCode,
        Integer amount,
        String currency,
        String status,
        String razorpayKey,
        String razorpayOrderId,
        String receipt,
        Instant paidAt,
        LocalDateTime createdAt
) {
}
