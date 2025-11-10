package com.tickets.backend.service;

import com.tickets.backend.dto.auth.TokenPayload;
import com.tickets.backend.model.User;
import com.tickets.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }
        String email = authentication.getName();
        return userRepository.findWithRolesByEmailIgnoreCase(email);
    }

    public User requireCurrentUser() {
        return getCurrentUser().orElseThrow(() -> new IllegalStateException("User not found in security context"));
    }

    public TokenPayload getCurrentPayload() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (details instanceof TokenPayload payload) {
            return payload;
        }
        return null;
    }
}
