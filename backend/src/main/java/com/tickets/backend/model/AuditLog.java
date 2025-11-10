package com.tickets.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "actorEmail", "action"})
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private UUID id;

    private String actorEmail;

    private String action;

    private String entityType;

    private UUID entityId;

    private String details;

    @Default
    private Instant createdAt = Instant.now();
}
