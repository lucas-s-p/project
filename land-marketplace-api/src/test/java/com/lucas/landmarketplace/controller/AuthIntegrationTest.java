package com.lucas.landmarketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.ObjectMapper;

import com.lucas.landmarketplace.dto.ForgotPasswordRequest;
import com.lucas.landmarketplace.dto.LoginRequest;
import com.lucas.landmarketplace.dto.RegisterRequest;
import com.lucas.landmarketplace.dto.ResetPasswordRequest;
import com.lucas.landmarketplace.model.User;
import com.lucas.landmarketplace.repository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@DisplayName("Auth end-to-end tests")
class AuthIntegrationTest {

    private static final String URI_REGISTER = "/api/auth/register";
    private static final String URI_LOGIN = "/api/auth/login";
    private static final String URI_VERIFY = "/api/auth/verify";
    private static final String URI_FORGOT_PASSWORD = "/api/auth/forgot-password";
    private static final String URI_RESET_PASSWORD = "/api/auth/reset-password";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    // Registration sends a real email via JavaMailSender; mocked here so tests don't depend
    // on a running SMTP server (Mailpit is only available when the app runs via Docker Compose).
    @MockitoBean
    JavaMailSender javaMailSender;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Registers an unverified user and sends a verification email")
    void testWhenWeRegister_createsUnverifiedUser_andSendsEmail() throws Exception {
        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("new@example.com", "password123"))))
                .andExpect(status().isOk())
                .andDo(print());

        User user = userRepository.findByEmail("new@example.com").orElseThrow();
        assertThat(user.isVerified()).isFalse();
        assertThat(user.getVerificationToken()).isNotBlank();
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Rejects registration with an email that is already in use")
    void testWhenWeRegisterWithDuplicateEmail_returns409() throws Exception {
        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("duplicate@example.com", "password123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("duplicate@example.com", "password123"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Rejects login before the email is verified")
    void testWhenWeLoginBeforeVerifying_returns403() throws Exception {
        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("unverified@example.com", "password123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post(URI_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("unverified@example.com", "password123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Verifies the email and then allows login, returning a JWT")
    void testWhenWeVerifyThenLogin_returnsToken() throws Exception {
        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("verified@example.com", "password123"))))
                .andExpect(status().isOk());

        String token = userRepository.findByEmail("verified@example.com").orElseThrow().getVerificationToken();

        mockMvc.perform(get(URI_VERIFY).param("token", token))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(post(URI_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("verified@example.com", "password123"))))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Rejects verification with an unknown token")
    void testWhenWeVerifyWithInvalidToken_returns400() throws Exception {
        mockMvc.perform(get(URI_VERIFY).param("token", "does-not-exist"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rejects login with the wrong password")
    void testWhenWeLoginWithWrongPassword_returns401() throws Exception {
        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("wrongpass@example.com", "password123"))))
                .andExpect(status().isOk());
        String token = userRepository.findByEmail("wrongpass@example.com").orElseThrow().getVerificationToken();
        mockMvc.perform(get(URI_VERIFY).param("token", token)).andExpect(status().isOk());

        mockMvc.perform(post(URI_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("wrongpass@example.com", "not-the-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sends a reset email for a registered address, and lets the user log in with the new password")
    void testWhenWeForgotPasswordThenReset_allowsLoginWithNewPassword() throws Exception {
        mockMvc.perform(post(URI_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("resetme@example.com", "password123"))))
                .andExpect(status().isOk());
        String verificationToken =
                userRepository.findByEmail("resetme@example.com").orElseThrow().getVerificationToken();
        mockMvc.perform(get(URI_VERIFY).param("token", verificationToken)).andExpect(status().isOk());

        mockMvc.perform(post(URI_FORGOT_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("resetme@example.com"))))
                .andExpect(status().isOk())
                .andDo(print());

        String resetToken = userRepository.findByEmail("resetme@example.com").orElseThrow().getResetToken();
        assertThat(resetToken).isNotBlank();

        mockMvc.perform(post(URI_RESET_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest(resetToken, "new-password456"))))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(post(URI_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("resetme@example.com", "new-password456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Returns the same generic message for forgot-password even when the email is unknown")
    void testWhenWeForgotPasswordForUnknownEmail_stillReturns200() throws Exception {
        mockMvc.perform(post(URI_FORGOT_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("nobody@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists for that email, a password reset link has been sent."));
    }

    @Test
    @DisplayName("Rejects password reset with an unknown token")
    void testWhenWeResetPasswordWithInvalidToken_returns400() throws Exception {
        mockMvc.perform(post(URI_RESET_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("does-not-exist", "new-password456"))))
                .andExpect(status().isBadRequest());
    }
}
