package com.learnify.paymentservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {

    private Long id;
    private Long studentId;
    private String studentEmail;
    private Long courseId;
    private String courseTitle;
    private Double amount;
    private String currency;
    private String mode;          // CARD, UPI, WALLET, etc.
    private String status;        // SUCCESS, FAILED, PENDING, REFUNDED
    private String transactionId;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}