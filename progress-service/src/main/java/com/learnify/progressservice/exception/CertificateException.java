package com.learnify.progressservice.exception;

// Thrown when certificate cannot be issued:
// - Course not yet 100% complete
// - Certificate already exists for this student+course
public class CertificateException extends RuntimeException {
    public CertificateException(String message) { super(message); }
}