package com.learnify.paymentservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Sent by frontend AFTER user completes payment in Razorpay UI.
 * Backend uses these 3 fields to verify the payment signature.
 * If signature is valid → payment is genuine → enroll student.
 */
@Data
public class PaymentVerifyRequest {
    @NotBlank(message = "razorpayOrderId is required")
    private String razorpayOrderId;    // From Razorpay order creation

    @NotBlank(message = "razorpayPaymentId is required")
    private String razorpayPaymentId;  // From Razorpay success callback

    @NotBlank(message = "razorpaySignature is required")
    private String razorpaySignature;  // From Razorpay success callback

    @NotNull(message = "courseId is required")
    private Long courseId;

    private String courseTitle;

    @NotNull(message = "amount is required")
    private Double amount;
}
