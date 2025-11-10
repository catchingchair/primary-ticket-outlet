package com.tickets.backend.service;

import com.tickets.backend.model.Venue;
import com.tickets.backend.repository.VenueRepository;
import com.tickets.backend.service.exception.VenueNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> findAll() {
        return venueRepository.findAll();
    }

    public Venue getById(UUID venueId) {
        return venueRepository.findById(venueId)
            .orElseThrow(() -> new VenueNotFoundException(venueId));
    }

    @Transactional
    public Venue createVenue(String name, String location, String description) {
        Venue venue = Venue.builder()
            .id(UUID.randomUUID())
            .name(name)
            .location(location)
            .description(description)
            .build();
        return venueRepository.save(venue);
    }
}
