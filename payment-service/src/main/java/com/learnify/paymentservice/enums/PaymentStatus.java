package com.learnify.paymentservice.enums;

public enum PaymentStatus {
    SUCCESS,    // Payment completed successfully
    FAILED,     // Payment attempt failed
    PENDING,    // Payment initiated but not yet confirmed
    REFUNDED    // Payment was refunded to student
}