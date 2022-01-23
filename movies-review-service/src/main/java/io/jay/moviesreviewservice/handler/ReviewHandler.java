package io.jay.moviesreviewservice.handler;

import io.jay.moviesreviewservice.domain.Review;
import io.jay.moviesreviewservice.exception.ReviewDataException;
import io.jay.moviesreviewservice.repository.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewHandler {

    private final ReviewReactiveRepository repository;
    private final Validator validator;
    private Sinks.Many<Review> reviewSink = Sinks.many().replay().latest();

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(repository::save)
                .doOnNext(review -> {
                    reviewSink.tryEmitNext(review);
                })
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        if (violations.size() > 0) {
            var message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ReviewDataException(message);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            return ServerResponse
                    .ok()
                    .body(repository.findByMovieInfoId(Long.valueOf(movieInfoId.get())), Review.class);
        } else {
            return ServerResponse
                    .ok()
                    .body(repository.findAll(), Review.class);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String id = request.pathVariable("id");
        var existingReview = repository.findById(id);
                /* option 1 for handling 404 */
                // .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for " + id)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(req -> {
                            review.setComment(req.getComment());
                            review.setRating(req.getRating());
                            return review;
                        })
                        .flatMap(repository::save)
                        .flatMap(ServerResponse.ok()::bodyValue))
                /* option 2 for handling 404 */
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String id = request.pathVariable("id");
        var existingReview = repository.findById(id);
        return existingReview
                .flatMap(review -> repository.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(reviewSink.asFlux(), Review.class);
    }
}
