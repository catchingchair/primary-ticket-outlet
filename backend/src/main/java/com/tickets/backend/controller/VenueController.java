package com.tickets.backend.controller;

import com.tickets.backend.dto.venue.CreateVenueRequest;
import com.tickets.backend.dto.venue.VenueResponse;
import com.tickets.backend.model.User;
import com.tickets.backend.service.AuditService;
import com.tickets.backend.service.CurrentUserService;
import com.tickets.backend.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public VenueController(VenueService venueService,
                           AuditService auditService,
                           CurrentUserService currentUserService) {
        this.venueService = venueService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> listVenues() {
        List<VenueResponse> venues = venueService.findAll().stream()
            .map(VenueResponse::fromModel)
            .toList();
        return ResponseEntity.ok(venues);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        User admin = currentUserService.requireCurrentUser();
        var venue = venueService.createVenue(request.name(), request.location(), request.description());
        auditService.log(admin.getEmail(), "VENUE_CREATED", "VENUE", venue.getId(), venue.getName());
        return ResponseEntity.ok(VenueResponse.fromModel(venue));
    }
}
