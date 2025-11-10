package com.tickets.backend.service.model;

import com.tickets.backend.model.Purchase;
import com.tickets.backend.model.Ticket;

import java.util.List;

public record PurchaseResult(Purchase purchase, List<Ticket> tickets) {
}
