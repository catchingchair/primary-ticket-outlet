package com.tickets.backend.service;

import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.VenueRepository;
import com.tickets.backend.service.exception.VenueNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    void createVenuePersistsAndReturns() {
        when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venue created = venueService.createVenue("Hall", "City", "Desc");

        assertThat(created.getId()).isNotNull();
        verify(venueRepository).save(created);
    }

    @Test
    void getByIdReturnsVenue() {
        UUID id = UUID.randomUUID();
        Venue venue = Venue.builder().id(id).name("Main").build();
        when(venueRepository.findById(id)).thenReturn(Optional.of(venue));

        assertThat(venueService.getById(id)).isEqualTo(venue);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(venueRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.getById(id))
            .isInstanceOf(VenueNotFoundException.class);
    }
}

