package com.tickets.backend.dto.admin;

import java.util.List;
import java.util.UUID;

public record VenueSummaryDto(
    UUID id,
    String name,
    String location,
    List<EventSummaryDto> events
) {
}

