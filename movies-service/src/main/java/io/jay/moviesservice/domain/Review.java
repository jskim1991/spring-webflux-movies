package io.jay.moviesservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class Review {

    private String reviewId;

    @NotNull(message = "review.movieInfoId must not be null")
    private Long movieInfoId;
    private String comment;

    @Min(value = 0L, message = "review.rating must be positive")
    private Double rating;
}
