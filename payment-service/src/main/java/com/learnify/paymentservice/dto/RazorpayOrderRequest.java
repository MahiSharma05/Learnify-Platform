package com.learnify.paymentservice.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request body when frontend asks backend to create a Razorpay order.
 * Frontend sends this before opening the Razorpay checkout UI.
 */
@Data
public class RazorpayOrderRequest {
    @NotNull(message = "courseId is required")
    private Long courseId;

    private String courseTitle;

    @NotNull(message = "amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount; // In INR (e.g., 499.0)

    private String currency = "INR";
}
