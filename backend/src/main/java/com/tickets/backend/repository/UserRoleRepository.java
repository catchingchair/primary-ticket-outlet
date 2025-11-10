package com.tickets.backend.repository;

import com.tickets.backend.model.UserRole;
import com.tickets.backend.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("select ur.venue from UserRole ur where ur.user.id = :userId and ur.role.name = 'ROLE_MANAGER'")
    List<Venue> findManagerVenues(UUID userId);

    @Query("""
        select ur from UserRole ur
        where ur.user.id = :userId
          and lower(ur.role.name) = lower(:roleName)
          and ur.venue.id = :venueId
    """)
    Optional<UserRole> findManagerAssignment(UUID userId, String roleName, UUID venueId);

    @Query("""
        select count(ur) > 0 from UserRole ur
        where ur.user.id = :userId
          and lower(ur.role.name) = lower(:roleName)
          and ur.venue is null
    """)
    boolean existsGlobalRole(UUID userId, String roleName);
}
