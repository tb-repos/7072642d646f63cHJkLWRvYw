package com.tourbhook.api.dto.auth;

public record OtpResponse(
        boolean verified,
        String message
) {
}
