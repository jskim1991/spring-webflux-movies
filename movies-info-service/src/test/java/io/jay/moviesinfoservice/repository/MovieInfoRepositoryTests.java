package io.jay.moviesinfoservice.repository;

import io.jay.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class MovieInfoRepositoryTests {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setup() {
        var movieInfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale"), LocalDate.parse("2008-07-18")),
                new MovieInfo("specific-id", "Dark Knight Rises", 2012, List.of("Christian Bale"), LocalDate.parse("2012-07-20"))
        );
        movieInfoRepository.saveAll(movieInfos)
                .blockLast(); // to make sure data is saved before start of tests
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll()
                .block();
    }

    @Test
    void findAll() {
        var flux = movieInfoRepository.findAll().log();
        StepVerifier.create(flux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        var mono = movieInfoRepository.findById("specific-id").log();
        StepVerifier.create(mono)
                .assertNext(movieInfo -> {
                    assertThat("specific-id", equalTo(movieInfo.getMovieInfoId()));
                    assertThat("Dark Knight Rises", equalTo(movieInfo.getName()));
                })
                .verifyComplete();
    }

    @Test
    void insert() {
        var movie = new MovieInfo(null, "Movie Title", 2021, List.of("First Last"), LocalDate.parse("2021-01-11"));
        var mono = movieInfoRepository.save(movie).log();
        StepVerifier.create(mono)
                .assertNext(movieInfo -> {
                    assertThat("Movie Title", equalTo(movieInfo.getName()));
                    assertThat(movieInfo.getMovieInfoId(), is(not(nullValue())));
                })
                .verifyComplete();
    }

    @Test
    void update() {
        var movie = movieInfoRepository.findById("specific-id").block();
        movie.setYear(2021);
        var mono = movieInfoRepository.save(movie).log();
        StepVerifier.create(mono)
                .assertNext(movieInfo -> {
                    assertThat(2021, equalTo(movieInfo.getYear()));
                });
    }

    @Test
    void delete() {
        movieInfoRepository.deleteById("specific-id").block();
        var flux = movieInfoRepository.findAll().log();
        StepVerifier.create(flux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear() {
        var flux = movieInfoRepository.findByYear(2005);
        StepVerifier.create(flux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {
        var mono = movieInfoRepository.findByName("Batman Begins");
        StepVerifier.create(mono)
                .expectNextCount(1)
                .verifyComplete();
    }
}
