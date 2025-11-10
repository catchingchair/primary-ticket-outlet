package com.tickets.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickets.backend.dto.venue.CreateVenueRequest;
import com.tickets.backend.model.User;
import com.tickets.backend.model.Venue;
import com.tickets.backend.service.AuditService;
import com.tickets.backend.service.CurrentUserService;
import com.tickets.backend.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VenueControllerTest {

    @Mock
    private VenueService venueService;

    @Mock
    private AuditService auditService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private VenueController venueController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Venue venue;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(venueController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        venue = Venue.builder()
            .id(UUID.randomUUID())
            .name("Main Hall")
            .location("City")
            .description("Desc")
            .build();
    }

    @Test
    void listVenuesReturnsCollection() throws Exception {
        when(venueService.findAll()).thenReturn(List.of(venue));

        mockMvc.perform(get("/api/venues"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Main Hall")));
    }

    @Test
    void createVenueCreatesWhenAdmin() throws Exception {
        User admin = User.builder()
            .id(UUID.randomUUID())
            .email("admin@example.com")
            .displayName("Admin")
            .build();
        when(currentUserService.requireCurrentUser()).thenReturn(admin);
        when(venueService.createVenue("New Venue", "Town", "Desc")).thenReturn(venue);

        CreateVenueRequest request = new CreateVenueRequest("New Venue", "Town", "Desc");

        mockMvc.perform(post("/api/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Main Hall")));

        verify(auditService).log("admin@example.com", "VENUE_CREATED", "VENUE", venue.getId(), venue.getName());
    }
}
