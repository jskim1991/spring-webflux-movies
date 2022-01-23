package io.jay.moviesreviewservice;

import io.jay.moviesreviewservice.domain.Review;
import io.jay.moviesreviewservice.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ReviewsIT {

    @Autowired
    WebTestClient client;

    @Autowired
    ReviewReactiveRepository repository;

    @BeforeEach
    void setup() {
        var reviews = List.of(
                new Review(null, 1L, "Awesome movie", 9.0),
                new Review(null, 1L, "Great movie", 8.0),
                new Review("specific-id", 2L, "Best movie", 9.0)
        );
        repository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Good movie", 7.5);
        client
                .post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var response = reviewEntityExchangeResult.getResponseBody();
                    assertThat(response, is(not(nullValue())));
                });
    }

    @Test
    void getReviews() {
        client
                .get()
                .uri("/v1/reviews")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        var review = new Review(null, null, "Updated review", 9.9);
        client
                .put()
                .uri("/v1/reviews/{id}", "specific-id")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var response = reviewEntityExchangeResult.getResponseBody();
                    assertThat(response.getComment(), equalTo("Updated review"));
                    assertThat(response.getRating(), equalTo(9.9));
                });
    }

    @Test
    void deleteReview() {
        client
                .delete()
                .uri("/v1/reviews/{id}", "specific-id")
                .exchange()
                .expectStatus()
                .isNoContent();

        var flux = repository.findAll();
        StepVerifier.create(flux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getReviews_byMovieInfoId() {
        var uri = UriComponentsBuilder.fromUriString("/v1/reviews")
                .queryParam("movieInfoId", 1L)
                .buildAndExpand()
                .toUri();
        client
                .get()
                .uri(uri)
                .exchange()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void getReviewsStream() {
        var review = new Review(null, 1L, "Comment to test this stream", 7.5);
        client
                .post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var response = reviewEntityExchangeResult.getResponseBody();
                    assertThat(response, is(not(nullValue())));
                });


        var reviewStreamFlux = client
                .get()
                .uri("/v1/reviews/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Review.class)
                .getResponseBody();


        StepVerifier.create(reviewStreamFlux.log())
                .assertNext(r -> {
                    assertThat(r.getComment(), equalTo("Comment to test this stream"));
                })
                .thenCancel();
    }
}
