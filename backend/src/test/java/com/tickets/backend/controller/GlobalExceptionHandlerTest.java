package com.tickets.backend.controller;

import com.tickets.backend.service.exception.EventNotFoundException;
import com.tickets.backend.service.exception.VenueNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundReturns404() {
        UUID eventId = UUID.randomUUID();
        var response = handler.handleNotFound(new EventNotFoundException(eventId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("code", "not_found");
    }

    @Test
    void handleBadRequestReturns400() {
        var response = handler.handleBadRequest(new IllegalArgumentException("invalid"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("code", "bad_request");
    }

    @Test
    void handleConflictReturns409() {
        var response = handler.handleConflict(new IllegalStateException("conflict"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("code", "conflict");
    }
}

