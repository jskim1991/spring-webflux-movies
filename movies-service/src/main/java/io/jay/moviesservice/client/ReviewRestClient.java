package io.jay.moviesservice.client;

import io.jay.moviesservice.domain.Review;
import io.jay.moviesservice.exception.ReviewsClientException;
import io.jay.moviesservice.exception.ReviewsServerException;
import io.jay.moviesservice.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReviewRestClient {

    private final WebClient client;

    @Value("${restClient.reviewsUrl}")
    private String reviewUrl;

    public Flux<Review> retrieveReviews(String movieId) {
        var uri = UriComponentsBuilder.fromHttpUrl(reviewUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        return client
                .get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new ReviewsClientException(message)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new ReviewsServerException(message)));
                })
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec(ReviewsServerException.class))
                .log();
    }
}
