package com.tickets.backend.dto.admin;

public record EventSummaryDto(
    java.util.UUID id,
    String title,
    String startsAt,
    int ticketsTotal,
    int ticketsSold,
    int revenueCents
) {
}

