package com.learnify.authservice.exception;

/**
 * Throw this when a requested resource does not exist in the database.
 * Example: userRepository.findById(id).orElseThrow(
 *     () -> new ResourceNotFoundException("User not found with id: " + id)
 * );
 *
 * The GlobalExceptionHandler will catch this and return HTTP 404 automatically.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}