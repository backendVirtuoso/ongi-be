package com.ongi.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OngiException extends RuntimeException {

    private final HttpStatus status;

    public OngiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static OngiException notFound(String message) {
        return new OngiException(message, HttpStatus.NOT_FOUND);
    }

    public static OngiException badRequest(String message) {
        return new OngiException(message, HttpStatus.BAD_REQUEST);
    }

    public static OngiException conflict(String message) {
        return new OngiException(message, HttpStatus.CONFLICT);
    }
}
