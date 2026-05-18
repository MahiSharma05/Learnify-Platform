package com.learnify.lessonservice.exception;

// Thrown when a lesson or resource is not found by ID
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}