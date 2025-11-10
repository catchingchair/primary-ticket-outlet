package com.tickets.backend.service;

import com.tickets.backend.model.Event;
import com.tickets.backend.model.Ticket;
import com.tickets.backend.model.TicketStatus;
import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.EventRepository;
import com.tickets.backend.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private EventService eventService;

    private Venue venue;

    @BeforeEach
    void setUp() {
        venue = Venue.builder()
            .id(UUID.randomUUID())
            .name("Venue")
            .location("City")
            .description("Desc")
            .build();
    }

    @Test
    void createEventPersistsValidatedEvent() {
        UUID venueId = venue.getId();
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.plusHours(3);
        when(venueService.getById(venueId)).thenReturn(venue);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(
            venueId,
            "My Event",
            "Great night",
            start,
            end,
            1500
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getVenue()).isEqualTo(venue);
        assertThat(result.getTitle()).isEqualTo("My Event");
        verify(eventRepository).save(result);
    }

    @Test
    void createEventRejectsInvalidEndTime() {
        when(venueService.getById(venue.getId())).thenReturn(venue);
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.minusHours(1);

        assertThatThrownBy(() -> eventService.createEvent(
            venue.getId(),
            "Bad Event",
            "invalid",
            start,
            end,
            2000
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateTicketsCreatesRequestedAmount() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
            .id(eventId)
            .venue(venue)
            .title("Night")
            .startsAt(OffsetDateTime.now().plusDays(5))
            .endsAt(OffsetDateTime.now().plusDays(5).plusHours(2))
            .faceValueCents(2500)
            .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Ticket> tickets = eventService.generateTickets(eventId, 3);

        assertThat(tickets).hasSize(3);
        assertThat(tickets).allMatch(ticket -> ticket.getStatus() == TicketStatus.AVAILABLE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ticket>> captor = (ArgumentCaptor<List<Ticket>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(ticketRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
    }

    @Test
    void reserveTicketsLocksAvailableTickets() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
            .id(eventId)
            .venue(venue)
            .title("Reserve test")
            .startsAt(OffsetDateTime.now().plusDays(5))
            .endsAt(OffsetDateTime.now().plusDays(5).plusHours(2))
            .faceValueCents(3000)
            .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        var available = List.of(
            Ticket.builder().id(UUID.randomUUID()).event(event).status(TicketStatus.AVAILABLE).code("AAA").build(),
            Ticket.builder().id(UUID.randomUUID()).event(event).status(TicketStatus.AVAILABLE).code("BBB").build()
        );
        when(ticketRepository.findTicketsForUpdate(eventId, TicketStatus.AVAILABLE, PageRequest.of(0, 2)))
            .thenReturn(available);

        List<Ticket> reserved = eventService.reserveTickets(eventId, 2);

        assertThat(reserved).hasSize(2)
            .allMatch(ticket -> ticket.getStatus() == TicketStatus.RESERVED);
        verify(ticketRepository).saveAll(available);
    }
}
