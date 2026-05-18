package com.learnify.discussionservice.exception;

// Thrown when a user tries to reply to a closed thread
public class ThreadClosedException extends RuntimeException {
    public ThreadClosedException(String message) { super(message); }
}