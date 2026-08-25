package com.shrinetours.api.service;

import com.shrinetours.api.dto.payment.InvoiceMetaResponse;

public interface InvoiceService {
    byte[] generateInvoicePdf(String orderId);
    InvoiceMetaResponse getInvoiceMeta(String orderId);
}
