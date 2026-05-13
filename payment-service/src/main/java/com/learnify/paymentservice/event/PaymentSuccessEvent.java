package com.learnify.paymentservice.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PaymentSuccessEvent — NEW FILE
 *
 * Published to RabbitMQ exchange "learnify.events"
 * Routing key: "payment.success"
 * Consumed by: notification-service (PaymentEventListener)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent implements Serializable{
    private Long paymentId;
    private Long studentId;
    private String studentEmail;
    private String studentName;

    /** Null for subscription payments */
    private Long courseId;
    private String courseTitle;

    private Double amount;
    private String currency;
    private String paymentMode;      // CARD, UPI, WALLET
    private String transactionId;

    /** COURSE_PURCHASE or SUBSCRIPTION */
    private String paymentType;

    /** Only set for SUBSCRIPTION payments */
    private String subscriptionPlan; // FREE, MONTHLY, ANNUAL

    private LocalDateTime paidAt;
}
