package io.jay.moviesservice.exception;

import lombok.Getter;

@Getter
public class ReviewsClientException extends RuntimeException {
    private String message;

    public ReviewsClientException(String message) {
        super(message);
        this.message = message;
    }
}
