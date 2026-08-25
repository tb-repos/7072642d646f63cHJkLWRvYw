package com.shrinetours.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @Email @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "OTP is required")
        String otp
) {
}
