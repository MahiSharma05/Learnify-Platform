package com.learnify.assessmentservice.exception;

// Thrown when student tries to attempt a quiz beyond the configured max attempts
public class AttemptLimitExceededException extends RuntimeException {
    public AttemptLimitExceededException(String message) { super(message); }
}