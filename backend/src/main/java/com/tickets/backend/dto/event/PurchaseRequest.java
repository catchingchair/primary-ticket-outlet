package com.tickets.backend.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PurchaseRequest(@Positive int quantity,
                              @NotBlank String paymentToken) {
}

