package com.tickets.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickets.backend.dto.auth.TokenPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthTokenServiceTest {

    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService(new ObjectMapper(), "test-secret");
        authTokenService.validateSecret();
    }

    @Test
    void generateAndParseRoundTrip() {
        TokenPayload payload = new TokenPayload("user@example.com", "User", List.of("ROLE_USER"));

        String token = authTokenService.generateToken(payload);
        TokenPayload parsed = authTokenService.parseToken(token);

        assertThat(parsed).isEqualTo(payload);
    }

    @Test
    void parseTokenRejectsInvalidSignature() {
        TokenPayload payload = new TokenPayload("user@example.com", "User", List.of("ROLE_USER"));
        String token = authTokenService.generateToken(payload);

        String tampered = token.replaceFirst(".$", token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> authTokenService.parseToken(tampered))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseTokenRejectsMalformedToken() {
        assertThatThrownBy(() -> authTokenService.parseToken("not-a-token"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

