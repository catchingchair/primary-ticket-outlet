package com.tickets.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record MockLoginRequest(
    @Email @NotBlank String email,
    @NotBlank String displayName,
    List<String> roles,
    List<UUID> managedVenueIds
) {

    public MockLoginRequest {
        roles = roles == null ? List.of() : List.copyOf(roles);
        managedVenueIds = managedVenueIds == null ? List.of() : List.copyOf(managedVenueIds);
    }
}
