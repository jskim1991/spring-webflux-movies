package io.jay.moviesreviewservice.repository;

import io.jay.moviesreviewservice.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactiveRepository extends ReactiveMongoRepository<Review, String> {
    Flux<Review> findByMovieInfoId(Long movieInfoId);
}
