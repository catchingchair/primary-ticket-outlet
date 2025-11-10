package com.tickets.backend.dto.payment;

import java.util.UUID;

public record PaymentRequest(
    UUID eventId,
    String userEmail,
    int amountCents,
    int quantity,
    String paymentToken
) {
}

