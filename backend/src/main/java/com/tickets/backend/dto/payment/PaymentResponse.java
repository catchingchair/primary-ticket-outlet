package com.tickets.backend.dto.payment;

public record PaymentResponse(
    boolean success,
    String reference,
    String message
) {

    public static PaymentResponse success(String reference) {
        return new PaymentResponse(true, reference, null);
    }

    public static PaymentResponse failure(String message) {
        return new PaymentResponse(false, null, message);
    }
}

