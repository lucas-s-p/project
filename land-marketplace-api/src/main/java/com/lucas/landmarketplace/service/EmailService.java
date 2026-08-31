package com.lucas.landmarketplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String verificationLink = frontendUrl + "/verify?token=" + verificationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("no-reply@plotline.local");
        message.setSubject("Verify your Plotline account");
        message.setText("""
                Welcome to Plotline!

                Please verify your email address by opening the link below:
                %s

                If you didn't create this account, you can ignore this email.
                """.formatted(verificationLink));

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("no-reply@plotline.local");
        message.setSubject("Reset your Plotline password");
        message.setText("""
                We received a request to reset your Plotline password.

                Open the link below to choose a new password:
                %s

                If you didn't request this, you can ignore this email.
                """.formatted(resetLink));

        mailSender.send(message);
    }
}
