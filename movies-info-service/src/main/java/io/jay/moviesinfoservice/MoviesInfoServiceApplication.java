package io.jay.moviesinfoservice;

import io.jay.moviesinfoservice.domain.MovieInfo;
import io.jay.moviesinfoservice.repository.MovieInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class MoviesInfoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoviesInfoServiceApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
class MoviesInfoRunner implements CommandLineRunner {

    private final MovieInfoRepository repository;

    @Override
    public void run(String... args) throws Exception {
        repository.deleteAll()
                .block();

        var movieInfos = List.of(
                new MovieInfo("1", "Batman Begins", 2005, List.of("Christian Bale"), LocalDate.parse("2005-06-15")),
                new MovieInfo("2", "The Dark Knight", 2008, List.of("Christian Bale"), LocalDate.parse("2008-07-18")),
                new MovieInfo("3", "Dark Knight Rises", 2012, List.of("Christian Bale"), LocalDate.parse("2012-07-20"))
        );

        repository.saveAll(movieInfos)
                .blockLast();
    }
}