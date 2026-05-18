package com.learnify.paymentservice.exception;

// Thrown for invalid payment operations:
// - Refunding an already-refunded payment
// - Cancelling an already-cancelled subscription
// - Duplicate transaction ID
public class PaymentException extends RuntimeException {
    public PaymentException(String message) { super(message); }
}