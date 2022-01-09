package io.jay.moviesinfoservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTests {

    @Autowired
    private WebTestClient client;

    @Test
    void flux() {
        client.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .contains(1, 2, 3)
                .hasSize(3);

        client.get()
                .uri("/flux")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                   var responseBody = listEntityExchangeResult.getResponseBody();
                   assertThat(3, equalTo(responseBody.size()));
                   assertThat(1, equalTo(responseBody.get(0)));
                   assertThat(2, equalTo(responseBody.get(1)));
                   assertThat(3, equalTo(responseBody.get(2)));
                });
    }

    @Test
    void mono() {
        var mono = client.get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(mono)
                .expectNext("hello world")
                .verifyComplete();
    }

    @Test
    void stream() {
        var response = client.get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(response)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();

    }
}