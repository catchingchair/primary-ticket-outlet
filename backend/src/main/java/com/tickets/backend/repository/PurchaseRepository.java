package com.tickets.backend.repository;

import com.tickets.backend.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Optional<Purchase> findByEventIdAndIdempotencyKey(UUID eventId, String idempotencyKey);

    @Query("""
        select p from Purchase p
        join fetch p.user u
        where p.event.id = :eventId
        order by p.createdAt asc
        """)
    List<Purchase> findAllByEventIdWithUser(UUID eventId);

    @Query("select coalesce(sum(p.totalAmountCents),0) from Purchase p where p.event.id = :eventId")
    Integer sumRevenueForEvent(UUID eventId);

    @Query("""
        select e.id, coalesce(sum(p.totalAmountCents),0) as revenue
        from Purchase p
        right join p.event e
        where e.venue.id = :venueId
        group by e.id
        """)
    List<Object[]> aggregateEventRevenue(UUID venueId);
}
