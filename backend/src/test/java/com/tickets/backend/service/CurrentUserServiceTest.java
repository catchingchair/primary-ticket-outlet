package com.tickets.backend.service;

import com.tickets.backend.dto.auth.TokenPayload;
import com.tickets.backend.model.User;
import com.tickets.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsPresentUser() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .displayName("User")
            .build();
        when(userRepository.findWithRolesByEmailIgnoreCase("user@example.com"))
            .thenReturn(Optional.of(user));

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("user@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(currentUserService.getCurrentUser()).contains(user);
    }

    @Test
    void getCurrentPayloadReturnsTokenPayload() {
        TokenPayload payload = new TokenPayload("user@example.com", "User", java.util.List.of("ROLE_USER"));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("user@example.com", null);
        authentication.setDetails(payload);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(currentUserService.getCurrentPayload()).isEqualTo(payload);
    }
}

