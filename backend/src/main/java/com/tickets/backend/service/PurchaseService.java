package com.tickets.backend.service;

import com.tickets.backend.dto.payment.PaymentRequest;
import com.tickets.backend.dto.payment.PaymentResponse;
import com.tickets.backend.model.Event;
import com.tickets.backend.model.Purchase;
import com.tickets.backend.model.Ticket;
import com.tickets.backend.model.TicketStatus;
import com.tickets.backend.model.User;
import com.tickets.backend.repository.PurchaseRepository;
import com.tickets.backend.repository.TicketRepository;
import com.tickets.backend.service.model.PurchaseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PurchaseService {

    private final EventService eventService;
    private final TicketRepository ticketRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentClient paymentClient;
    private final AuditService auditService;

    public PurchaseService(EventService eventService,
                           TicketRepository ticketRepository,
                           PurchaseRepository purchaseRepository,
                           PaymentClient paymentClient,
                           AuditService auditService) {
        this.eventService = eventService;
        this.ticketRepository = ticketRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentClient = paymentClient;
        this.auditService = auditService;
    }

    @Transactional
    public PurchaseResult purchaseTickets(User user,
                                          UUID eventId,
                                          int quantity,
                                          String paymentToken,
                                          String idempotencyKey) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        return purchaseRepository.findByEventIdAndIdempotencyKey(eventId, idempotencyKey)
            .map(existing -> new PurchaseResult(existing, ticketRepository.findByPurchaseId(existing.getId())))
            .orElseGet(() -> performPurchase(user, eventId, quantity, paymentToken, idempotencyKey));
    }

    private PurchaseResult performPurchase(User user,
                                           UUID eventId,
                                           int quantity,
                                           String paymentToken,
                                           String idempotencyKey) {
        Event event = eventService.getById(eventId);
        List<Ticket> reservedTickets = eventService.reserveTickets(eventId, quantity);
        try {
            int totalAmount = event.getFaceValueCents() * quantity;
            PaymentResponse payment = paymentClient.charge(new PaymentRequest(
                eventId,
                user.getEmail(),
                totalAmount,
                quantity,
                paymentToken
            ));
            if (!payment.success()) {
                throw new IllegalStateException(payment.message() != null ? payment.message() : "Payment failed");
            }

            Purchase purchase = purchaseRepository.save(Purchase.builder()
                .id(UUID.randomUUID())
                .event(event)
                .user(user)
                .quantity(quantity)
                .totalAmountCents(totalAmount)
                .paymentReference(payment.reference())
                .idempotencyKey(idempotencyKey)
                .build());

            reservedTickets.forEach(ticket -> {
                ticket.setStatus(TicketStatus.SOLD);
                ticket.setPurchase(purchase);
            });
            ticketRepository.saveAll(reservedTickets);
            eventService.markTicketsSold(event, quantity);
            auditService.log(user.getEmail(), "PURCHASE_CONFIRMED", "EVENT", eventId, "quantity=" + quantity);
            return new PurchaseResult(purchase, reservedTickets);
        } catch (RuntimeException ex) {
            reservedTickets.forEach(ticket -> ticket.setStatus(TicketStatus.AVAILABLE));
            ticketRepository.saveAll(reservedTickets);
            throw ex;
        }
    }
}
