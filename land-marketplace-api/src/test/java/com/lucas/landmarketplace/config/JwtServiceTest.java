package com.lucas.landmarketplace.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("test-secret-key-with-at-least-256-bits-of-entropy!!", 60_000);

    @Test
    void generateToken_andExtractEmail_roundTrips() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void isValid_returnsTrue_forFreshToken() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalse_forGarbageToken() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void isValid_returnsFalse_forExpiredToken() throws InterruptedException {
        JwtService shortLivedJwtService =
                new JwtService("test-secret-key-with-at-least-256-bits-of-entropy!!", 1);
        String token = shortLivedJwtService.generateToken("user@example.com");

        Thread.sleep(10);

        assertThat(shortLivedJwtService.isValid(token)).isFalse();
    }
}
