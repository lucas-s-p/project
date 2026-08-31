package com.lucas.landmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUnverifiedUser_andSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password123");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        MessageResponse response = authService.register(request);

        assertThat(response.message()).contains("check your email");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("new@example.com"), anyString());
    }

    @Test
    void register_throws_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void verifyEmail_marksUserAsVerified_whenTokenIsValid() {
        User user = User.builder()
                .verificationToken("valid-token")
                .verificationTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .verified(false)
                .build();
        when(userRepository.findByVerificationToken("valid-token")).thenReturn(Optional.of(user));

        MessageResponse response = authService.verifyEmail("valid-token");

        assertThat(response.message()).contains("verified");
        assertThat(user.isVerified()).isTrue();
        assertThat(user.getVerificationToken()).isNull();
    }

    @Test
    void verifyEmail_throws_whenTokenDoesNotExist() {
        when(userRepository.findByVerificationToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("missing"))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    void verifyEmail_throws_whenTokenIsExpired() {
        User user = User.builder()
                .verificationToken("expired-token")
                .verificationTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .verified(false)
                .build();
        when(userRepository.findByVerificationToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail("expired-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        when(jwtService.generateToken("user@example.com")).thenReturn("a-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("a-jwt-token");
    }

    @Test
    void login_throwsEmailNotVerified_whenAccountIsDisabled() {
        LoginRequest request = new LoginRequest("unverified@example.com", "password123");
        org.mockito.Mockito.doThrow(new DisabledException("disabled"))
                .when(authenticationManager)
                .authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void forgotPassword_sendsResetEmail_whenAccountExists() {
        User user = User.builder().email("user@example.com").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        MessageResponse response = authService.forgotPassword(new ForgotPasswordRequest("user@example.com"));

        assertThat(response.message()).contains("password reset link");
        assertThat(user.getResetToken()).isNotBlank();
        verify(emailService).sendPasswordResetEmail(eq("user@example.com"), anyString());
    }

    @Test
    void forgotPassword_returnsSameMessage_andSendsNoEmail_whenAccountDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        MessageResponse response = authService.forgotPassword(new ForgotPasswordRequest("missing@example.com"));

        assertThat(response.message()).contains("password reset link");
        org.mockito.Mockito.verifyNoInteractions(emailService);
    }

    @Test
    void resetPassword_updatesPasswordHash_whenTokenIsValid() {
        User user = User.builder()
                .resetToken("valid-reset-token")
                .resetTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        when(userRepository.findByResetToken("valid-reset-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password123")).thenReturn("new-hashed");

        MessageResponse response = authService.resetPassword(
                new ResetPasswordRequest("valid-reset-token", "new-password123"));

        assertThat(response.message()).contains("Password updated");
        assertThat(user.getPasswordHash()).isEqualTo("new-hashed");
        assertThat(user.getResetToken()).isNull();
    }

    @Test
    void resetPassword_throws_whenTokenDoesNotExist() {
        when(userRepository.findByResetToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("missing", "new-password123")))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
    }

    @Test
    void resetPassword_throws_whenTokenIsExpired() {
        User user = User.builder()
                .resetToken("expired-reset-token")
                .resetTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
        when(userRepository.findByResetToken("expired-reset-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("expired-reset-token", "new-password123")))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
    }
}
