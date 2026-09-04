package com.tourbhook.api.service;

public interface EmailService {

    default void sendOtpEmail(String to, String otp) {
        sendOtpEmail(to, otp, "en");
    }

    void sendOtpEmail(String to, String otp, String languageCode);
}