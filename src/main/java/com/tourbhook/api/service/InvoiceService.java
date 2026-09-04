package com.tourbhook.api.service;

import com.tourbhook.api.dto.payment.InvoiceMetaResponse;

public interface InvoiceService {
    byte[] generateInvoicePdf(String orderId);
    InvoiceMetaResponse getInvoiceMeta(String orderId);
}
