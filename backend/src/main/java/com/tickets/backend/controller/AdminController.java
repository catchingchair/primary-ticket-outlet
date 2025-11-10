package com.tickets.backend.controller;

import com.tickets.backend.dto.admin.EventSummaryDto;
import com.tickets.backend.dto.admin.VenueSummaryDto;
import com.tickets.backend.model.Event;
import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.PurchaseRepository;
import com.tickets.backend.service.EventService;
import com.tickets.backend.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VenueService venueService;
    private final EventService eventService;
    private final PurchaseRepository purchaseRepository;

    public AdminController(VenueService venueService,
                           EventService eventService,
                           PurchaseRepository purchaseRepository) {
        this.venueService = venueService;
        this.eventService = eventService;
        this.purchaseRepository = purchaseRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<VenueSummaryDto>> dashboard() {
        List<VenueSummaryDto> venues = venueService.findAll().stream()
            .map(this::mapVenue)
            .toList();
        return ResponseEntity.ok(venues);
    }

    private VenueSummaryDto mapVenue(Venue venue) {
        List<EventSummaryDto> events = eventService.listByVenue(venue.getId()).stream()
            .map(this::mapEvent)
            .toList();
        return new VenueSummaryDto(venue.getId(), venue.getName(), venue.getLocation(), events);
    }

    private EventSummaryDto mapEvent(Event event) {
        Integer revenue = purchaseRepository.sumRevenueForEvent(event.getId());
        return new EventSummaryDto(
            event.getId(),
            event.getTitle(),
            event.getStartsAt().toString(),
            event.getTicketsTotal(),
            event.getTicketsSold(),
            revenue == null ? 0 : revenue
        );
    }
}
