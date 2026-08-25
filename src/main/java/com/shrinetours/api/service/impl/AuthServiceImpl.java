//package com.shrinetours.api.service.impl;
//
//import com.shrinetours.api.dto.auth.*;
//import com.shrinetours.api.entity.OtpCode;
//import com.shrinetours.api.entity.User;
//import com.shrinetours.api.exception.BadRequestException;
//import com.shrinetours.api.repository.OtpCodeRepository;
//import com.shrinetours.api.repository.UserRepository;
//import com.shrinetours.api.security.JwtService;
//import com.shrinetours.api.service.AnalyticsService;
//import com.shrinetours.api.service.AuthService;
//import com.shrinetours.api.service.EmailService;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.security.SecureRandom;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class AuthServiceImpl implements AuthService {
//
//    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//    private final UserRepository userRepository;
//    private final OtpCodeRepository otpCodeRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final AnalyticsService analyticsService;
//    private final JwtService jwtService;
//    private final EmailService emailService;
//    @Override
//    public AuthResponse login(LoginRequest request) {
//        log.info("Processing login for email={}", request.email());
//        User user = userRepository.findByEmailIgnoreCase(request.email())
//                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
//
//        if (user.isDeleted() || !passwordEncoder.matches(request.password(), user.getPassword())) {
//            throw new BadRequestException("Invalid email or password");
//        }
//
//        analyticsService.track("login", Map.of("email", user.getEmail()));
//        return buildAuthResponse(user);
//    }
//
//    @Override
//    public AuthResponse register(RegisterRequest request) {
//        log.info("Processing registration for email={}", request.email());
//        if (userRepository.existsByEmailIgnoreCase(request.email())) {
//            throw new BadRequestException("Email already registered");
//        }
//
//        User user = userRepository.save(
//                User.builder()
//                        .name(request.name())
//                        .email(request.email())
//                        .phone(request.phone())
//                        .password(passwordEncoder.encode(request.password()))
//                        .dob(LocalDate.of(1995, 1, 1))
//                        .avatarUrl("https://cdn.shrinetours.local/avatar/default.png")
//                        .level("Explorer")
//                        .levelProgress(new BigDecimal("0.10"))
//                        .tripsCompleted(0)
//                        .premium(false)
//                        .deleted(false)
//                        .build()
//        );
//
//        issueFreshOtp(user.getEmail());
//
//        analyticsService.track("register", Map.of("email", user.getEmail()));
//        return buildAuthResponse(user);
//    }
//
//    @Override
//    public AuthResponse googleLogin(GoogleAuthRequest request) {
//        log.info("Processing google sign-in for email={}", request.email());
//        User user = userRepository.findByEmailIgnoreCase(request.email())
//                .orElseGet(() -> userRepository.save(
//                        User.builder()
//                                .name(request.name())
//                                .email(request.email())
//                                .phone("")
//                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
//                                .dob(LocalDate.of(1995, 1, 1))
//                                .avatarUrl("https://cdn.shrinetours.local/avatar/google.png")
//                                .level("Explorer")
//                                .levelProgress(new BigDecimal("0.10"))
//                                .tripsCompleted(0)
//                                .premium(false)
//                                .deleted(false)
//                                .build()
//                ));
//
//        analyticsService.track("google_login", Map.of("email", user.getEmail()));
//        return buildAuthResponse(user);
//    }
//    @Override
//    public TokenResponse refresh(RefreshRequest request) {
//        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
//            throw new BadRequestException("Refresh token is required");
//        }
//
//        if (!jwtService.isTokenValid(request.refreshToken()) || !jwtService.isRefreshToken(request.refreshToken())) {
//            throw new BadRequestException("Invalid refresh token");
//        }
//
//        String subject = jwtService.extractSubject(request.refreshToken());
//        String newAccessToken = jwtService.generateAccessToken(subject);
//
//        return new TokenResponse(
//                newAccessToken,
//                request.refreshToken(),
//                jwtService.getAccessTokenExpirationSeconds()
//        );
//    }
//
//    @Override
//    public OtpResponse verifyOtp(VerifyOtpRequest request) {
//        OtpCode code = otpCodeRepository.findTopByEmailAndCodeAndConsumedFalseOrderByExpiresAtDesc(request.email(), request.otp())
//                .orElseThrow(() -> new BadRequestException("Invalid OTP"));
//
//        if (code.getExpiresAt().isBefore(Instant.now())) {
//            throw new BadRequestException("OTP expired");
//        }
//
//        code.setConsumed(true);
//        otpCodeRepository.save(code);
//        return new OtpResponse(true, "OTP verified successfully");
//    }
//
//    @Override
//    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
//        User user = userRepository.findByEmailIgnoreCase(request.email())
//                .orElseThrow(() -> new BadRequestException("User not found with this email"));
//
//        issueFreshOtp(user.getEmail());
//        analyticsService.track("forgot_password", Map.of("email", user.getEmail()));
//        return new OtpResponse(true, "OTP sent successfully");
//    }
//
//    @Override
//    public OtpResponse resendOtp(ResendOtpRequest request) {
//        User user = userRepository.findByEmailIgnoreCase(request.email())
//                .orElseThrow(() -> new BadRequestException("User not found with this email"));
//
//        issueFreshOtp(user.getEmail());
//        analyticsService.track("otp_resent", Map.of("email", user.getEmail()));
//        return new OtpResponse(true, "OTP resent successfully");
//    }
//
//    @Override
//    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
//        if (!request.password().equals(request.confirm_password())) {
//            throw new BadRequestException("Password and confirm password must match");
//        }
//
//        User user = userRepository.findByEmailIgnoreCase(request.email())
//                .orElseThrow(() -> new BadRequestException("User not found with this email"));
//
//        user.setPassword(passwordEncoder.encode(request.password()));
//        userRepository.save(user);
//
//        analyticsService.track("password_reset", Map.of("email", user.getEmail()));
//        return Map.of("ok", true, "message", "Password reset successfully");
//    }
//
//    @Override
//    public Map<String, Object> logout() {
//        log.info("Processing logout");
//        return Map.of("ok", true, "message", "Session cleared successfully");
//    }
//
//    private void issueFreshOtp(String email) {
//        var activeCodes = otpCodeRepository.findByEmailAndConsumedFalse(email);
//        activeCodes.forEach(existing -> existing.setConsumed(true));
//        otpCodeRepository.saveAll(activeCodes);
//
//        String otp = generateOtp();
//
//        otpCodeRepository.save(
//                OtpCode.builder()
//                        .email(email)
//                        .code(otp)
//                        .expiresAt(Instant.now().plusSeconds(300))
//                        .consumed(false)
//                        .build()
//        );
//
//        emailService.sendOtpEmail(email, otp);
//    }
////    private void issueFreshOtp(String email) {
////        var activeCodes = otpCodeRepository.findByEmailAndConsumedFalse(email);
////        activeCodes.forEach(existing -> existing.setConsumed(true));
////        otpCodeRepository.saveAll(activeCodes);
////
////        String otp = generateOtp();
////
////        otpCodeRepository.save(
////                OtpCode.builder()
////                        .email(email)
////                        .code(otp)
////                        .expiresAt(Instant.now().plusSeconds(300))
////                        .consumed(false)
////                        .build()
////        );
////
////        log.info("Generated OTP for {} is {}", email, otp);
////    }
//
//    private String generateOtp() {
//        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
//    }
//
//    private AuthResponse buildAuthResponse(User user) {
//        String accessToken = jwtService.generateAccessToken(user.getEmail());
//        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
//        return new AuthResponse(
//                accessToken,
//                refreshToken,
//                jwtService.getAccessTokenExpirationSeconds(),
//                new AuthUserResponse(
//                        user.getId(),
//                        user.getName(),
//                        user.getEmail(),
//                        user.getPhone(),
//                        user.isPremium()
//                )
//        );
//    }
//}
package com.shrinetours.api.service.impl;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shrinetours.api.dto.auth.AuthResponse;
import com.shrinetours.api.dto.auth.AuthUserResponse;
import com.shrinetours.api.dto.auth.ForgotPasswordRequest;
import com.shrinetours.api.dto.auth.GoogleAuthRequest;
import com.shrinetours.api.dto.auth.LoginRequest;
import com.shrinetours.api.dto.auth.OtpResponse;
import com.shrinetours.api.dto.auth.RefreshRequest;
import com.shrinetours.api.dto.auth.RegisterRequest;
import com.shrinetours.api.dto.auth.ResendOtpRequest;
import com.shrinetours.api.dto.auth.ResetPasswordRequest;
import com.shrinetours.api.dto.auth.TokenResponse;
import com.shrinetours.api.dto.auth.VerifyOtpRequest;
import com.shrinetours.api.entity.OtpCode;
import com.shrinetours.api.entity.User;
import com.shrinetours.api.repository.OtpCodeRepository;
import com.shrinetours.api.repository.UserRepository;
import com.shrinetours.api.repository.exception.BadRequestException;
import com.shrinetours.api.security.JwtService;
import com.shrinetours.api.service.AnalyticsService;
import com.shrinetours.api.service.AuthService;
import com.shrinetours.api.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticsService analyticsService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email={}", request.email());
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.isDeleted() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        analyticsService.track("login", Map.of("email", user.getEmail()));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email={}", request.email());
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already registered");
        }

        User user = userRepository.save(
                User.builder()
                        .name(request.name())
                        .email(request.email())
                        .phone(request.phone())
                        .password(passwordEncoder.encode(request.password()))
                        .dob(LocalDate.of(1995, 1, 1))
                        .avatarUrl("https://cdn.shrinetours.local/avatar/default.png")
                        .level("Explorer")
                        .levelProgress(new BigDecimal("0.10"))
                        .tripsCompleted(0)
                        .premium(false)
                        .deleted(false)
                        .build()
        );

        issueFreshOtp(user.getEmail());

        analyticsService.track("register", Map.of("email", user.getEmail()));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        log.info("Processing google sign-in for email={}", request.email());
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(request.name())
                                .email(request.email())
                                .phone("")
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .dob(LocalDate.of(1995, 1, 1))
                                .avatarUrl("https://cdn.shrinetours.local/avatar/google.png")
                                .level("Explorer")
                                .levelProgress(new BigDecimal("0.10"))
                                .tripsCompleted(0)
                                .premium(false)
                                .deleted(false)
                                .build()
                ));

        analyticsService.track("google_login", Map.of("email", user.getEmail()));
        return buildAuthResponse(user);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new BadRequestException("Refresh token is required");
        }

        if (!jwtService.isTokenValid(request.refreshToken()) || !jwtService.isRefreshToken(request.refreshToken())) {
            throw new BadRequestException("Invalid refresh token");
        }

        String subject = jwtService.extractSubject(request.refreshToken());
        User user = userRepository.findById(subject)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() -> new BadRequestException("User not found for refresh token"));

        String newAccessToken = jwtService.generateAccessToken(user.getId());

        return new TokenResponse(
                newAccessToken,
                request.refreshToken(),
                jwtService.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpCode code = otpCodeRepository.findTopByEmailAndCodeAndConsumedFalseOrderByExpiresAtDesc(request.email(), request.otp())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (code.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("OTP expired");
        }

        code.setConsumed(true);
        otpCodeRepository.save(code);
        return new OtpResponse(true, "OTP verified successfully");
    }

    @Override
    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("User not found with this email"));

        issueFreshOtp(user.getEmail());
        analyticsService.track("forgot_password", Map.of("email", user.getEmail()));
        return new OtpResponse(true, "OTP sent successfully");
    }

    @Override
    public OtpResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("User not found with this email"));

        issueFreshOtp(user.getEmail());
        analyticsService.track("otp_resent", Map.of("email", user.getEmail()));
        return new OtpResponse(true, "OTP resent successfully");
    }

    @Override
    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        if (!request.password().equals(request.confirm_password())) {
            throw new BadRequestException("Password and confirm password must match");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("User not found with this email"));

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        analyticsService.track("password_reset", Map.of("email", user.getEmail()));
        return Map.of("ok", true, "message", "Password reset successfully");
    }

    @Override
    public Map<String, Object> logout() {
        log.info("Processing logout");
        return Map.of("ok", true, "message", "Session cleared successfully");
    }

    private void issueFreshOtp(String email) {
        var activeCodes = otpCodeRepository.findByEmailAndConsumedFalse(email);
        activeCodes.forEach(existing -> existing.setConsumed(true));
        otpCodeRepository.saveAll(activeCodes);

        String otp = generateOtp();

        otpCodeRepository.save(
                OtpCode.builder()
                        .email(email)
                        .code(otp)
                        .expiresAt(Instant.now().plusSeconds(300))
                        .consumed(false)
                        .build()
        );

        emailService.sendOtpEmail(email, otp);
    }

    private String generateOtp() {
        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                new AuthUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.isPremium()
                )
        );
    }
}
