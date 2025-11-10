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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private EventService eventService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PurchaseService purchaseService;

    private Event event;
    private User user;

    @BeforeEach
    void setUp() {
        event = Event.builder()
            .id(UUID.randomUUID())
            .venue(VenueServiceTestFixtures.venue())
            .title("Show")
            .startsAt(OffsetDateTime.now().plusDays(3))
            .endsAt(OffsetDateTime.now().plusDays(3).plusHours(2))
            .faceValueCents(5000)
            .ticketsTotal(10)
            .ticketsSold(0)
            .build();
        user = User.builder()
            .id(UUID.randomUUID())
            .email("buyer@example.com")
            .displayName("Buyer")
            .build();
    }

    @Test
    void purchaseTicketsCreatesPurchaseAndUpdatesTickets() {
        UUID eventId = event.getId();
        when(eventService.getById(eventId)).thenReturn(event);
        List<Ticket> reserved = List.of(ticket("A"), ticket("B"));
        when(eventService.reserveTickets(eventId, 2)).thenReturn(reserved);
        when(paymentClient.charge(any(PaymentRequest.class)))
            .thenReturn(PaymentResponse.success("ref-1"));
        when(purchaseRepository.findByEventIdAndIdempotencyKey(eventId, "id-key"))
            .thenReturn(Optional.empty());
        when(purchaseRepository.save(any(Purchase.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseResult result = purchaseService.purchaseTickets(
            user,
            eventId,
            2,
            "token",
            "id-key"
        );

        assertThat(result.purchase().getQuantity()).isEqualTo(2);
        assertThat(result.tickets()).hasSize(2)
            .allMatch(ticket -> ticket.getStatus() == TicketStatus.SOLD);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(purchaseCaptor.capture());
        assertThat(purchaseCaptor.getValue().getPaymentReference()).isEqualTo("ref-1");
        verify(auditService).log(eq(user.getEmail()), eq("PURCHASE_CONFIRMED"), eq("EVENT"), eq(eventId), eq("quantity=2"));
    }

    @Test
    void purchaseTicketsRestoresTicketsWhenPaymentFails() {
        UUID eventId = event.getId();
        when(eventService.getById(eventId)).thenReturn(event);
        List<Ticket> reserved = List.of(ticket("A"));
        when(eventService.reserveTickets(eventId, 1)).thenReturn(reserved);
        when(purchaseRepository.findByEventIdAndIdempotencyKey(eventId, "id-key"))
            .thenReturn(Optional.empty());
        when(paymentClient.charge(any(PaymentRequest.class)))
            .thenReturn(PaymentResponse.failure("declined"));

        assertThatThrownBy(() -> purchaseService.purchaseTickets(
            user,
            eventId,
            1,
            "token",
            "id-key"
        )).isInstanceOf(IllegalStateException.class);

        verify(ticketRepository).saveAll(reserved);
    }

    private Ticket ticket(String code) {
        return Ticket.builder()
            .id(UUID.randomUUID())
            .event(event)
            .status(TicketStatus.AVAILABLE)
            .code(code)
            .build();
    }

    private static final class VenueServiceTestFixtures {
        private static final java.util.concurrent.atomic.AtomicInteger COUNTER = new java.util.concurrent.atomic.AtomicInteger(1);

        static com.tickets.backend.model.Venue venue() {
            int idx = COUNTER.getAndIncrement();
            return com.tickets.backend.model.Venue.builder()
                .id(UUID.randomUUID())
                .name("Venue " + idx)
                .location("City")
                .description("Desc")
                .build();
        }
    }
}

