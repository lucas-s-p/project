package com.lucas.landmarketplace.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucas.landmarketplace.config.JwtService;
import com.lucas.landmarketplace.dto.AuthResponse;
import com.lucas.landmarketplace.dto.ForgotPasswordRequest;
import com.lucas.landmarketplace.dto.LoginRequest;
import com.lucas.landmarketplace.dto.MessageResponse;
import com.lucas.landmarketplace.dto.RegisterRequest;
import com.lucas.landmarketplace.dto.ResetPasswordRequest;
import com.lucas.landmarketplace.exception.EmailAlreadyRegisteredException;
import com.lucas.landmarketplace.exception.EmailNotVerifiedException;
import com.lucas.landmarketplace.exception.InvalidPasswordResetTokenException;
import com.lucas.landmarketplace.exception.InvalidVerificationTokenException;
import com.lucas.landmarketplace.model.User;
import com.lucas.landmarketplace.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long VERIFICATION_TOKEN_TTL_HOURS = 24;
    private static final long RESET_TOKEN_TTL_HOURS = 1;
    private static final String FORGOT_PASSWORD_RESPONSE_MESSAGE =
            "If an account exists for that email, a password reset link has been sent.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        String verificationToken = UUID.randomUUID().toString();
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .verified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiresAt(Instant.now().plus(VERIFICATION_TOKEN_TTL_HOURS, ChronoUnit.HOURS))
                .build();
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return new MessageResponse("Account created. Please check your email to verify it before logging in.");
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (user.getVerificationTokenExpiresAt() == null
                || user.getVerificationTokenExpiresAt().isBefore(Instant.now())) {
            throw new InvalidVerificationTokenException();
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);

        return new MessageResponse("Email verified. You can now log in.");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (DisabledException ex) {
            throw new EmailNotVerifiedException();
        }

        return new AuthResponse(jwtService.generateToken(request.email()));
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiresAt(Instant.now().plus(RESET_TOKEN_TTL_HOURS, ChronoUnit.HOURS));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        });

        // Always return the same message, regardless of whether the email is registered,
        // so this endpoint can't be used to discover which emails have an account.
        return new MessageResponse(FORGOT_PASSWORD_RESPONSE_MESSAGE);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new InvalidPasswordResetTokenException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);

        return new MessageResponse("Password updated. You can now log in with your new password.");
    }
}
