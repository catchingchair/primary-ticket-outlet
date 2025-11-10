package com.tickets.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickets.backend.dto.auth.TokenPayload;
import com.tickets.backend.util.MessageDigestHelper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Component
public class AuthTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final String secret;

    public AuthTokenService(ObjectMapper objectMapper, @Value("${security.auth-token.secret:changeme}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.auth-token.secret must not be blank");
        }
    }

    public String generateToken(TokenPayload payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadEncoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signatureEncoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sign(payloadEncoded));
            return payloadEncoded + "." + signatureEncoded;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate auth token", e);
        }
    }

    public TokenPayload parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }

        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Token format invalid");
        }

        String payloadPart = parts[0];
        String signaturePart = parts[1];

        byte[] expectedSignature = sign(payloadPart);
        byte[] providedSignature = Base64.getUrlDecoder().decode(signaturePart);

        if (!MessageDigestHelper.isEqual(expectedSignature, providedSignature)) {
            throw new IllegalArgumentException("Token signature invalid");
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadPart);
            return objectMapper.readValue(payloadBytes, TokenPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token payload invalid", e);
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to sign auth token", e);
        }
    }
}

