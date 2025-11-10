package com.tickets.backend.dto.event;

import com.tickets.backend.model.Ticket;
import com.tickets.backend.service.model.PurchaseResult;

import java.util.List;
import java.util.UUID;

public record PurchaseResponse(UUID purchaseId,
                               String paymentReference,
                               int quantity,
                               int totalAmountCents,
                               List<String> ticketCodes) {
    public static PurchaseResponse from(PurchaseResult result) {
        return new PurchaseResponse(
            result.purchase().getId(),
            result.purchase().getPaymentReference(),
            result.purchase().getQuantity(),
            result.purchase().getTotalAmountCents(),
            result.tickets().stream().map(Ticket::getCode).toList()
        );
    }
}
