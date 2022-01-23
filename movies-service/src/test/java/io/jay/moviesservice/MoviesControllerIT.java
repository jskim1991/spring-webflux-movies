package io.jay.moviesservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.jay.moviesservice.domain.Movie;
import io.jay.moviesservice.domain.MovieInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        })
public class MoviesControllerIT {

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setup() {
        WireMock.reset();
    }

    @Test
    void retrieveMovieById() {
        stubFor(get(urlEqualTo("/v1/movieinfos/123"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=123"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("reviews.json")));


        client
                .get()
                .uri("/v1/movies/{id}", "123")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assertThat(movie.getMovieInfo().getName(), equalTo("Batman Begins"));
                    assertThat(movie.getReviews().size(), equalTo(2));
                });
    }

    @Test
    void retrieveMovieById_movieInfo_404() {
        stubFor(get(urlEqualTo("/v1/movieinfos/123"))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=123"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("reviews.json")));


        client
                .get()
                .uri("/v1/movies/{id}", "123")
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("No movie info for id 123");

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos/123")));
    }

    @Test
    void retrieveMovieById_reviews_404() {
        stubFor(get(urlEqualTo("/v1/movieinfos/123"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=123"))
                .willReturn(aResponse()
                        .withStatus(404)));


        client
                .get()
                .uri("/v1/movies/{id}", "123")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assertThat(movie.getMovieInfo().getName(), equalTo("Batman Begins"));
                    assertThat(movie.getReviews().size(), equalTo(0));
                });
    }

    @Test
    void retrieveMovieById_movieInfo_500() {
        stubFor(get(urlEqualTo("/v1/movieinfos/123"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")));


        client
                .get()
                .uri("/v1/movies/{id}", "123")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("MovieInfo Service Unavailable");
    }

    @Test
    void retrieveMovieById_reviews_500() {
        stubFor(get(urlEqualTo("/v1/movieinfos/123"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=123"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Reviews Service Unavailable")));


        client
                .get()
                .uri("/v1/movies/{id}", "123")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Reviews Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/reviews?movieInfoId=123")));
    }

    @Test
    void retrieveMovieById_reviews_retry() {
        stubFor(get(urlEqualTo("/v1/movieinfos/123"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")));


        client
                .get()
                .uri("/v1/movies/{id}", "123")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("MovieInfo Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos/123")));
    }

    @Test
    void retrieveMovieInfoStream() throws JsonProcessingException {
        MovieInfo movieInfo = new MovieInfo("999", "A New Hope", 2012, List.of("Actor1"), LocalDate.parse("2012-01-01"));
        stubFor(get(urlEqualTo("/v1/movieinfos/stream"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                        .withBody(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(movieInfo))
                )
        );


        var streamFlux = client
                .get()
                .uri("/v1/movies/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class)
                .getResponseBody();


        StepVerifier.create(streamFlux.log())
                .assertNext(m -> {
                    assertThat(m.getName(), equalTo("A New Hope"));
                })
                .thenCancel();
    }
}
