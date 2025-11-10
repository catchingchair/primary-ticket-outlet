package com.tickets.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title"})
@Entity
@Table(name = "events")
public class Event {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    private Venue venue;

    private String title;

    private String description;

    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    private int faceValueCents;

    @Default
    private int ticketsTotal = 0;

    @Default
    private int ticketsSold = 0;

    @Default
    private Instant createdAt = Instant.now();

    @Default
    private Instant updatedAt = Instant.now();
}
