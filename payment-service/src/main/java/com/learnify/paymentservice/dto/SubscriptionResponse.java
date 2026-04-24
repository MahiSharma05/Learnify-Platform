package com.learnify.paymentservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SubscriptionResponse {

    private Long id;
    private Long studentId;
    private String studentEmail;
    private String plan;            // FREE, MONTHLY, ANNUAL
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;          // ACTIVE, CANCELLED, EXPIRED
    private Double amountPaid;
    private boolean autoRenew;
    private Long paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    // Computed field: is the subscription currently valid?
    private boolean active;

    // Days remaining until expiry
    private long daysRemaining;
}