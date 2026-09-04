package com.tourbhook.api.dto.auth;

public record AuthUserResponse(
        String id,
        String name,
        String email,
        String phone,
        boolean premium
) {
}
