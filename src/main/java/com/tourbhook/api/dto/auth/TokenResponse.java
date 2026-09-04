package com.tourbhook.api.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
