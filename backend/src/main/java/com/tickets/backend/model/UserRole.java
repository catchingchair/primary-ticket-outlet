package com.tickets.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"id"})
@Entity
@Table(name = "user_roles",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_role_venue", columnNames = {"user_id", "role_id", "venue_id"}))
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false)
    @EqualsAndHashCode.Include
    private User user;

    @ManyToOne(optional = false)
    @EqualsAndHashCode.Include
    private Role role;

    @ManyToOne
    @EqualsAndHashCode.Include
    private Venue venue;
}
