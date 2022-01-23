package io.jay.moviesreviewservice;

import io.jay.moviesreviewservice.domain.Review;
import io.jay.moviesreviewservice.exceptionhandler.GlobalExceptionHandler;
import io.jay.moviesreviewservice.handler.ReviewHandler;
import io.jay.moviesreviewservice.repository.ReviewReactiveRepository;
import io.jay.moviesreviewservice.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalExceptionHandler.class})
public class ReviewRouterTests {

    @MockBean
    ReviewReactiveRepository repository;

    @Autowired
    WebTestClient client;

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Good movie", 7.5);


        when(repository.save(review))
                .thenReturn(Mono.just(new Review("id", 1L, "Good movie", 7.5)));


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
        when(repository.findAll())
                .thenReturn(Flux.just(new Review("id", 1L, "Good movie", 7.5)));


        client
                .get()
                .uri("/v1/reviews")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void updateReview() {
        var requestBody = new Review(null, null, "Great movie", 9.5);
        var existingReview = new Review("id", 1L, "Good movie", 7.5);
        var updatedReview = new Review("id", 1L, "Great movie", 9.5);
        when(repository.findById("id"))
                .thenReturn(Mono.just(existingReview));
        when(repository.save(updatedReview))
                .thenReturn(Mono.just(updatedReview));


        client
                .put()
                .uri("/v1/reviews/{id}", "id")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var response = reviewEntityExchangeResult.getResponseBody();
                    assertThat(response.getRating(), equalTo(9.5));
                    assertThat(response.getComment(), equalTo("Great movie"));
                });
    }

    @Test
    void deleteReview() {
        var existingReview = new Review("id", 1L, "Good movie", 7.5);
        when(repository.findById("id"))
                .thenReturn(Mono.just(existingReview));
        when(repository.deleteById("id"))
                .thenReturn(Mono.empty().ofType(Void.class));


        client
                .delete()
                .uri("/v1/reviews/{id}", "id")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviews_byMovieInfoId() {
        var existingReview = new Review("id", 1L, "Good movie", 7.5);
        when(repository.findByMovieInfoId(1L))
                .thenReturn(Flux.just(existingReview));


        client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/reviews")
                        .queryParam("movieInfoId", 1L)
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(1);
    }

    @Test
    void addReview_validation() {
        var review = new Review(null, null, "Good movie", -1.0);


        client
                .post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("review.movieInfoId must not be null, review.rating must be positive");
    }
}
