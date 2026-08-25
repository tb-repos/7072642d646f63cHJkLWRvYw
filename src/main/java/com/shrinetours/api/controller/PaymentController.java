package com.shrinetours.api.controller;

import com.shrinetours.api.dto.common.ApiResponse;
import com.shrinetours.api.dto.payment.*;
import com.shrinetours.api.service.PaymentGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(@Valid @RequestBody CreatePaymentOrderRequest request) {
        PaymentOrderResponse response = paymentGatewayService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.<PaymentOrderResponse>builder()
                .success(true)
                .message("Payment order created successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .path("/api/v1/payments/create-order")
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyPaymentResponse>> verify(@Valid @RequestBody VerifyPaymentRequest request) {
        VerifyPaymentResponse response = paymentGatewayService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.<VerifyPaymentResponse>builder()
                .success(true)
                .message("Payment verified successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .path("/api/v1/payments/verify")
                .build());
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<PaymentWebhookResponse>> webhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload
    ) {
        PaymentWebhookResponse response = paymentGatewayService.processWebhook(signature, payload);
        return ResponseEntity.ok(ApiResponse.<PaymentWebhookResponse>builder()
                .success(true)
                .message("Webhook processed successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .path("/api/v1/payments/webhook")
                .build());
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> getOrders() {
        List<PaymentOrderResponse> response = paymentGatewayService.getMyOrders();
        return ResponseEntity.ok(ApiResponse.<List<PaymentOrderResponse>>builder()
                .success(true)
                .message("Payment orders fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .path("/api/v1/payments/orders")
                .build());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> getOrder(@PathVariable String id) {
        PaymentOrderResponse response = paymentGatewayService.getOrder(id);
        return ResponseEntity.ok(ApiResponse.<PaymentOrderResponse>builder()
                .success(true)
                .message("Payment order fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .path("/api/v1/payments/orders/" + id)
                .build());
    }

    @GetMapping("/orders/{id}/invoice/meta")
    public ResponseEntity<ApiResponse<InvoiceMetaResponse>> getInvoiceMeta(@PathVariable String id) {
        InvoiceMetaResponse response = paymentGatewayService.getInvoiceMeta(id);
        return ResponseEntity.ok(ApiResponse.<InvoiceMetaResponse>builder()
                .success(true)
                .message("Invoice meta fetched successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .path("/api/v1/payments/orders/" + id + "/invoice/meta")
                .build());
    }

    @GetMapping("/orders/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String id) {
        byte[] pdf = paymentGatewayService.downloadInvoice(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("invoice-" + id + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}