package com.learnify.paymentservice.entity;

import com.learnify.paymentservice.enums.PaymentMode;
import com.learnify.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student who made the payment
    @Column(nullable = false)
    private Long studentId;

    private String studentEmail;

    // Course being purchased (null for subscription payments)
    private Long courseId;
    private String courseTitle;

    // Payment amount in INR (or configured currency)
    @Column(nullable = false)
    private Double amount;

    // Currency code e.g. "INR", "USD"
    @Column(nullable = false)
    private String currency = "INR";

    // CARD, UPI, WALLET, NET_BANKING, FREE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode mode;

    // SUCCESS, FAILED, PENDING, REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // Unique transaction ID from payment gateway (mocked here)
    @Column(unique = true)
    private String transactionId;

    // Reason for failure (if status = FAILED)
    private String failureReason;

    @CreationTimestamp
    private LocalDateTime paidAt;

    // Set when admin processes a refund
    private LocalDateTime refundedAt;
}