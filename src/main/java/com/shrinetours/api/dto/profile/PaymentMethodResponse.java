package com.shrinetours.api.dto.profile;

public record PaymentMethodResponse(
        String id,
        String type,
        String lastFour,
        String holderName,
        String expiry,
        Boolean primaryMethod
) {
}
