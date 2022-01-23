package io.jay.moviesinfoservice.service;

import io.jay.moviesinfoservice.domain.MovieInfo;
import io.jay.moviesinfoservice.repository.MovieInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MoviesInfoService {

    private final MovieInfoRepository repository;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return repository.save(movieInfo).log();
    }

    public Flux<MovieInfo> getAllMovieInfos() {
        return repository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return repository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {
        return repository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setRelease_date(updatedMovieInfo.getRelease_date());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    return repository.save(movieInfo);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return repository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfosByYear(Integer year) {
        return repository.findByYear(year);
    }
}
