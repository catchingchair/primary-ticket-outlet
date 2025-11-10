package com.tickets.backend.dto.event;

import com.tickets.backend.model.Event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponse(UUID id,
                            UUID venueId,
                            String venueName,
                            String title,
                            String description,
                            OffsetDateTime startsAt,
                            OffsetDateTime endsAt,
                            int faceValueCents,
                            int ticketsTotal,
                            int ticketsSold) {
    public static EventResponse fromModel(Event entity) {
        return new EventResponse(
            entity.getId(),
            entity.getVenue().getId(),
            entity.getVenue().getName(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getStartsAt(),
            entity.getEndsAt(),
            entity.getFaceValueCents(),
            entity.getTicketsTotal(),
            entity.getTicketsSold()
        );
    }
}
