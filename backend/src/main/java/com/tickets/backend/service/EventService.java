package com.tickets.backend.service;

import com.tickets.backend.model.Event;
import com.tickets.backend.model.Ticket;
import com.tickets.backend.model.TicketStatus;
import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.EventRepository;
import com.tickets.backend.repository.TicketRepository;
import com.tickets.backend.service.exception.EventNotFoundException;
import com.tickets.backend.util.TicketCodeGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final VenueService venueService;

    public EventService(EventRepository eventRepository,
                        TicketRepository ticketRepository,
                        VenueService venueService) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.venueService = venueService;
    }

    public List<Event> listAll() {
        return eventRepository.findAllWithVenue();
    }

    public List<Event> listUpcoming() {
        return eventRepository.findUpcoming(OffsetDateTime.now());
    }

    public List<Event> listByVenue(UUID venueId) {
        return eventRepository.findByVenueId(venueId);
    }

    public Event getById(UUID eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional
    public Event createEvent(UUID venueId,
                             String title,
                             String description,
                             OffsetDateTime startsAt,
                             OffsetDateTime endsAt,
                             int faceValueCents) {
        Venue venue = venueService.getById(venueId);
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }
        if (faceValueCents <= 0) {
            throw new IllegalArgumentException("faceValueCents must be positive");
        }

        Event event = Event.builder()
            .id(UUID.randomUUID())
            .venue(venue)
            .title(title)
            .description(description)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .faceValueCents(faceValueCents)
            .build();

        return eventRepository.save(event);
    }

    @Transactional
    public List<Ticket> generateTickets(UUID eventId, int quantity) {
        if (quantity <= 0 || quantity > 5000) {
            throw new IllegalArgumentException("quantity must be between 1 and 5000");
        }
        Event event = getById(eventId);
        List<Ticket> generated = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .event(event)
                .status(TicketStatus.AVAILABLE)
                .code(TicketCodeGenerator.generateCode())
                .build();
            generated.add(ticket);
        }
        ticketRepository.saveAll(generated);
        event.setTicketsTotal(event.getTicketsTotal() + quantity);
        return generated;
    }

    @Transactional
    public void markTicketsSold(Event event, int quantity) {
        event.setTicketsSold(event.getTicketsSold() + quantity);
        eventRepository.save(event);
    }

    @Transactional
    public List<Ticket> reserveTickets(UUID eventId, int quantity) {
        Event event = getById(eventId);
        List<Ticket> tickets = ticketRepository.findTicketsForUpdate(
            eventId,
            TicketStatus.AVAILABLE,
            PageRequest.of(0, quantity)
        );
        if (tickets.size() < quantity) {
            throw new IllegalStateException("Insufficient tickets available");
        }
        tickets.forEach(ticket -> ticket.setStatus(TicketStatus.RESERVED));
        ticketRepository.saveAll(tickets);
        return tickets;
    }
}
