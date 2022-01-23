package io.jay.moviesservice.exception;

import lombok.Getter;

@Getter
public class MoviesInfoServerException extends RuntimeException {
    private String message;

    public MoviesInfoServerException(String message) {
        super(message);
        this.message = message;
    }
}
