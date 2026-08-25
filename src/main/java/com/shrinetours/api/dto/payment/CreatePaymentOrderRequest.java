package com.shrinetours.api.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record CreatePaymentOrderRequest(
        @NotBlank(message = "planCode is required")
        String planCode
) {
}
