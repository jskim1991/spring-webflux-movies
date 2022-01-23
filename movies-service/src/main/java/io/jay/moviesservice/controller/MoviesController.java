package io.jay.moviesservice.controller;

import io.jay.moviesservice.client.MoviesInfoRestClient;
import io.jay.moviesservice.client.ReviewRestClient;
import io.jay.moviesservice.domain.Movie;
import io.jay.moviesservice.domain.MovieInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewRestClient reviewRestClient;

    @GetMapping("/{movieId}")
    public Mono<Movie> retrieveMovieById(@PathVariable String movieId) {
        return moviesInfoRestClient.retrieveMovieInfo(movieId)
                .flatMap(movieInfo -> {
                    var reviewsMono = reviewRestClient.retrieveReviews(movieInfo.getMovieInfoId())
                            .collectList();
                    return reviewsMono.map(reviews -> new Movie(movieInfo, reviews));
                });
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MovieInfo> retrieveMovieInfos() {
        return moviesInfoRestClient.retrieveMovieInfoStream();
    }
}
