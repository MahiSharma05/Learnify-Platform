package com.learnify.enrollment.exception;

// Thrown when a student tries to enroll in a course they are already enrolled in
public class DuplicateEnrollmentException extends RuntimeException {
    public DuplicateEnrollmentException(String message) {
        super(message);
    }
}