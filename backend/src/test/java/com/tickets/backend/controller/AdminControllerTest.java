package com.tickets.backend.controller;

import com.tickets.backend.model.Event;
import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.PurchaseRepository;
import com.tickets.backend.service.EventService;
import com.tickets.backend.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private VenueService venueService;

    @Mock
    private EventService eventService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private Venue venue;
    private Event event;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        venue = Venue.builder()
            .id(UUID.randomUUID())
            .name("Main Hall")
            .location("City")
            .build();
        event = Event.builder()
            .id(UUID.randomUUID())
            .venue(venue)
            .title("Concert")
            .startsAt(OffsetDateTime.now().plusDays(3))
            .endsAt(OffsetDateTime.now().plusDays(3).plusHours(2))
            .faceValueCents(4000)
            .ticketsTotal(100)
            .ticketsSold(20)
            .build();
    }

    @Test
    void dashboardReturnsVenueSummary() throws Exception {
        when(venueService.findAll()).thenReturn(List.of(venue));
        when(eventService.listByVenue(venue.getId())).thenReturn(List.of(event));
        when(purchaseRepository.sumRevenueForEvent(event.getId())).thenReturn(80000);

        mockMvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Main Hall")))
            .andExpect(jsonPath("$[0].events[0].revenueCents", is(80000)));
    }
}
