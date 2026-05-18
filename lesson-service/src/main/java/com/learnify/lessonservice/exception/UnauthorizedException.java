package com.learnify.lessonservice.exception;

// Thrown when a user tries to modify a resource they don't own
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}