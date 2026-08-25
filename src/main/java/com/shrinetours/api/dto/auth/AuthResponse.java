package com.shrinetours.api.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AuthUserResponse user
) {
}
