package com.learnify.paymentservice.entity;

import com.learnify.paymentservice.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    private String studentEmail;

    // FREE, MONTHLY, ANNUAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    // Subscription validity window
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // ACTIVE, CANCELLED, EXPIRED
    @Column(nullable = false)
    private String status = "ACTIVE";

    // Amount paid for this subscription (0 for FREE plan)
    @Column(nullable = false)
    private Double amountPaid = 0.0;

    // true = automatically renew before endDate
    @Column(nullable = false)
    private boolean autoRenew = true;

    // Links to the payment record that funded this subscription
    private Long paymentId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Set when admin cancels the subscription
    private LocalDateTime cancelledAt;
}