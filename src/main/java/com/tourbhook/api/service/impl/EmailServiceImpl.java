package com.tourbhook.api.service.impl;

import com.tourbhook.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.tourbhook.api.service.MessageService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MessageService messageService;

    @Override
    public void sendOtpEmail(String to, String otp, String languageCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(messageService.getForLanguage("otp.email.subject", languageCode));
        message.setText(messageService.getForLanguage("otp.email.body", languageCode, otp));
        mailSender.send(message);
    }
}