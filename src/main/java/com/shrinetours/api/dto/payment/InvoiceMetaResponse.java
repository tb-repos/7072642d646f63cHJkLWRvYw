package com.shrinetours.api.dto.payment;

import java.time.Instant;

public record InvoiceMetaResponse(
        String orderId,
        String invoiceNumber,
        String receipt,
        String planCode,
        Integer amount,
        String currency,
        String status,
        Instant paidAt,
        Instant issuedAt,
        String downloadUrl
) {
}