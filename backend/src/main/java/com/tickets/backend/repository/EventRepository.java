package com.tickets.backend.repository;

import com.tickets.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("select e from Event e join fetch e.venue v order by e.startsAt asc")
    List<Event> findAllWithVenue();

    @Query("select e from Event e where e.venue.id = :venueId order by e.startsAt asc")
    List<Event> findByVenueId(UUID venueId);

    @Query("select e from Event e where e.startsAt >= :now order by e.startsAt asc")
    List<Event> findUpcoming(OffsetDateTime now);
}
