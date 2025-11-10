package com.tickets.backend.dto.venue;

import com.tickets.backend.model.Venue;

import java.util.UUID;

public record VenueResponse(UUID id,
                            String name,
                            String location,
                            String description) {
    public static VenueResponse fromModel(Venue entity) {
        return new VenueResponse(
            entity.getId(),
            entity.getName(),
            entity.getLocation(),
            entity.getDescription()
        );
    }
}
