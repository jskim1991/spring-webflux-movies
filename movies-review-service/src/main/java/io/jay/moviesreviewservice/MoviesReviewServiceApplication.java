package io.jay.moviesreviewservice;

import io.jay.moviesreviewservice.domain.Review;
import io.jay.moviesreviewservice.repository.ReviewReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
public class MoviesReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoviesReviewServiceApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
class ReviewRunner implements CommandLineRunner {

    private final ReviewReactiveRepository repository;

    @Override
    public void run(String... args) throws Exception {

        repository.deleteAll()
                .block();

        var random = new Random();
        var reviews = new ArrayList<Review>();

        for (int i = 0; i < 100; i++) {
            var movieId = random.nextInt(3 - 1 + 1) + 1;
            var rating = Math.round(((10 - 1) * random.nextDouble() + 1) * 10) / 10.0;
            var review = new Review(null, (long) movieId, UUID.randomUUID().toString(), rating);
            reviews.add(review);
        }

        repository.saveAll(reviews)
                .blockLast();
    }
}
