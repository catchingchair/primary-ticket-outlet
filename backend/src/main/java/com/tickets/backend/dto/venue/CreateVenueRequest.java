package com.tickets.backend.dto.venue;

import jakarta.validation.constraints.NotBlank;

public record CreateVenueRequest(@NotBlank String name,
                                 String location,
                                 String description) {
}

