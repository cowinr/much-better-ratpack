package com.example.muchbetter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import ratpack.http.HttpMethod;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.MainClassApplicationUnderTest;
import ratpack.test.http.TestHttpClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RatpackConfigTest {

    private static MainClassApplicationUnderTest appUnderTest = new MainClassApplicationUnderTest(MuchBetterApplication.class);

    @Test
    public void userIsCreatedAndReturnsToken() {
        ReceivedResponse response = appUnderTest.getHttpClient().post("login");
        assertEquals(201, response.getStatus().getCode());
        assertNotNull(response.getHeaders().get("Authorization"));
    }

    @Test
    public void sendTransactionForValidTokenReturnsCreated() throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Create new user
        TestHttpClient httpClient = appUnderTest.getHttpClient();
        ReceivedResponse loginResponse = httpClient.post("login");
        String auth = loginResponse.getHeaders().get("Authorization");

        // New user spends something
        Transaction transaction = new Transaction(LocalDateTime.now(), "Item desc", new BigDecimal("1.24"), "GBP");

        ReceivedResponse spendResponse = httpClient.request("spend", req -> {
            req
                .headers(mutableHeaders -> mutableHeaders
                    .set("Authorization", "Bearer " + auth)
                    .set("Content-Type", "application/json"))
                .method(HttpMethod.POST)
                .body(body -> {
                    body.text(objectMapper.writeValueAsString(transaction));
                });
        });

        assertEquals(201, spendResponse.getStatus().getCode());
    }
}