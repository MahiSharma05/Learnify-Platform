package com.learnify.paymentservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent back to frontend after creating a Razorpay order.
 * Frontend uses orderId + keyId to open the Razorpay checkout UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String orderId;       // e.g., order_Abc123XYZ
    private String currency;      // INR
    private Long   amount;        // Amount in PAISE (multiply INR by 100)
    private String keyId;         // Razorpay key ID (safe to expose to frontend)
    private Long   courseId;
    private String courseTitle;
}
