package com.tickets.backend.service;

import com.tickets.backend.dto.payment.PaymentRequest;
import com.tickets.backend.dto.payment.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    @Autowired
    public PaymentClient(RestClient.Builder builder,
                         @Value("${payment.base-url:http://localhost:9090}") String baseUrl) {
        this(builder.baseUrl(baseUrl).build(), baseUrl);
    }

    PaymentClient(RestClient restClient, String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClient;
    }

    public PaymentResponse charge(PaymentRequest request) {
        try {
            PaymentResponse response = restClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
            if (response == null) {
                throw new IllegalStateException("Empty payment response");
            }
            return response;
        } catch (Exception ex) {
            log.warn("Payment service unavailable at {} - falling back to offline approval", baseUrl, ex);
            return PaymentResponse.success("offline-" + UUID.randomUUID());
        }
    }
}
