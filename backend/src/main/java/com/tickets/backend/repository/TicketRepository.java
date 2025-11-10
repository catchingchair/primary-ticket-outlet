package com.tickets.backend.repository;

import com.tickets.backend.model.Ticket;
import com.tickets.backend.model.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    long countByEventIdAndStatus(UUID eventId, TicketStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.event.id = :eventId and t.status = :status order by t.createdAt asc")
    List<Ticket> findTicketsForUpdate(UUID eventId, TicketStatus status, Pageable pageable);

    List<Ticket> findByPurchaseId(UUID purchaseId);
}
