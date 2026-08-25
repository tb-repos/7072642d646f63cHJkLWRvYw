package com.shrinetours.api.dto.auth;

public record OtpResponse(
        boolean verified,
        String message
) {
}
