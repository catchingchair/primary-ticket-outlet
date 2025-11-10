package com.tickets.backend.dto.event;

public record PurchaserResponse(String email,
                                String displayName,
                                int quantity,
                                int totalAmountCents,
                                String purchasedAt) {
}

