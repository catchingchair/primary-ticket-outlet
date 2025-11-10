package com.tickets.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickets.backend.dto.payment.PaymentRequest;
import com.tickets.backend.dto.payment.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PaymentClientTest {

    private static final String BASE_URL = "http://localhost:9090";

    private PaymentClient paymentClient;
    private MockRestServiceServer server;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(restClientBuilder).ignoreExpectOrder(true).build();
        RestClient restClient = restClientBuilder.build();
        paymentClient = new PaymentClient(restClient, BASE_URL);
        objectMapper = new ObjectMapper();
    }

    @Test
    void chargeReturnsServiceResponse() throws Exception {
        PaymentResponse expected = PaymentResponse.success("ref-123");
        server.expect(requestTo(BASE_URL + "/api/payments"))
            .andExpect(method(POST))
            .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        PaymentResponse response = paymentClient.charge(sampleRequest());

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void chargeFallsBackWhenServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/api/payments"))
            .andRespond(withServerError());

        PaymentResponse response = paymentClient.charge(sampleRequest());

        assertThat(response.success()).isTrue();
        assertThat(response.reference()).startsWith("offline-");
    }

    private PaymentRequest sampleRequest() {
        return new PaymentRequest(
            UUID.randomUUID(),
            "user@example.com",
            1000,
            1,
            "token"
        );
    }
}
