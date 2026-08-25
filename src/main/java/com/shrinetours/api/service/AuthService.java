package com.shrinetours.api.service;

import com.shrinetours.api.dto.auth.*;

import java.util.Map;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse googleLogin(GoogleAuthRequest request);
    TokenResponse refresh(RefreshRequest request);
    OtpResponse verifyOtp(VerifyOtpRequest request);
    OtpResponse forgotPassword(ForgotPasswordRequest request);
    OtpResponse resendOtp(ResendOtpRequest request);
    Map<String, Object> resetPassword(ResetPasswordRequest request);
    Map<String, Object> logout();
}
