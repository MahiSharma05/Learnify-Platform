package com.learnify.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    // Course being purchased
    @NotNull(message = "courseId is required")
    private Long courseId;

    private String courseTitle;

    @NotNull(message = "amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    private String currency = "INR";

    // CARD, UPI, WALLET, NET_BANKING, FREE
    @NotBlank(message = "Payment mode is required (CARD, UPI, WALLET, NET_BANKING, FREE)")
    private String mode;

    /**
     * In a real system this would be a gateway token (Razorpay orderId etc.)
     * Here we accept a mock transaction ID for simulation.
     * If null, the service will auto-generate one.
     */
    private String transactionId;
}