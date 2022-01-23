package io.jay.moviesservice.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoviesInfoClientException extends RuntimeException {
    private String message;
    private int status;

    public MoviesInfoClientException(String message, int status) {
        super(message);
        this.message = message;
        this.status = status;
    }
}
