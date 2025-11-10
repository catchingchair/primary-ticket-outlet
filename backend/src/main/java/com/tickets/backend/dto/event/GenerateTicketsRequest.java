package com.tickets.backend.dto.event;

import jakarta.validation.constraints.Positive;

public record GenerateTicketsRequest(@Positive int quantity) {
}

