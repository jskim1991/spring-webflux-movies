package io.jay.moviesinfoservice.controller;

import io.jay.moviesinfoservice.domain.MovieInfo;
import io.jay.moviesinfoservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
class MoviesInfoControllerIT {

    @Autowired
    WebTestClient client;

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
    void addMovieInfo() {
        var movie = new MovieInfo(null, "Movie Title", 2021, List.of("First Last"), LocalDate.parse("2021-01-11"));

        client.post()
                .uri("/v1/movieinfos")
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var saved = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(saved.getMovieInfoId(), is(not(nullValue())));
                });
    }

    @Test
    void getAllMovieInfos() {
        client.get()
                .uri("/v1/movieinfos")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        client.get()
                .uri("/v1/movieinfos/{id}", "specific-id")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void updateMovieInfo() {
        var updateInfo = new MovieInfo(null, "new movie", 2022, List.of("actor"), LocalDate.parse("2022-01-12"));
        client.put()
                .uri("/v1/movieinfos/{id}", "specific-id")
                .bodyValue(updateInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var response = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(response.getMovieInfoId(), is(not(nullValue())));
                    assertThat(response.getName(), equalTo("new movie"));
                    assertThat(response.getYear(), equalTo(2022));
                    assertThat(response.getCast().size(), equalTo(1));
                    assertThat(response.getRelease_date(), equalTo(LocalDate.parse("2022-01-12")));
                });
    }

    @Test
    void deleteMovieInfo() {
        client.delete()
                .uri("/v1/movieinfos/{id}", "specific-id")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);
    }

    @Test
    void updateMovieInfo_notFound() {
        var updateInfo = new MovieInfo(null, "new movie", 2022, List.of("actor"), LocalDate.parse("2022-01-12"));
        client.put()
                .uri("/v1/movieinfos/{id}", "non-existing-id")
                .bodyValue(updateInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfoById_notFound() {
        client.get()
                .uri("/v1/movieinfos/{id}", "non-existing-id")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfosByYear() {
        var uri = UriComponentsBuilder.fromUriString("/v1/movieinfos")
                .queryParam("year", 2012)
                .buildAndExpand().toUri();
        client
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoStream() {
        var movie = new MovieInfo(null, "Movie Title", 2021, List.of("First Last"), LocalDate.parse("2021-01-11"));
        client.post()
                .uri("/v1/movieinfos")
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var saved = movieInfoEntityExchangeResult.getResponseBody();
                    assertThat(saved.getMovieInfoId(), is(not(nullValue())));
                });


        var moviesStreamFlux = client
                .get()
                .uri("/v1/movieinfos/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class)
                .getResponseBody();


        StepVerifier.create(moviesStreamFlux.log())
                .assertNext(movieInfo -> {
                    assertThat(movieInfo.getMovieInfoId(), is(not(nullValue())));
                })
                .thenCancel();
    }
}