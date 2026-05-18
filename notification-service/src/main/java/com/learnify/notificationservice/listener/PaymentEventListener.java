package com.learnify.notificationservice.listener;
import com.learnify.notificationservice.dto.NotificationRequest;
import com.learnify.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PaymentEventListener — NEW FILE
 *
 * Consumes PAYMENT_SUCCESS events from RabbitMQ queue:
 *   learnify.payment.queue
 *
 * Handles two payment types:
 *  - COURSE_PURCHASE   → "Payment Successful" notification with course details
 *  - SUBSCRIPTION      → "Subscription Activated" notification with plan details
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final NotificationService notificationService;

    /**
     * Triggered when a payment or subscription is successfully processed.
     */
    @RabbitListener(queues = "learnify.payment.queue")
    public void handlePaymentSuccess(@Payload Map<String, Object> event) {

        log.info("[PaymentListener] Received PAYMENT_SUCCESS event: {}", event);

        try {
            Long   studentId    = toLong(event.get("studentId"));
            String studentEmail = (String) event.get("studentEmail");
            String studentName  = (String) event.get("studentName");
            String paymentType  = (String) event.get("paymentType");
            Object amountObj    = event.get("amount");
            String amount       = amountObj != null ? String.format("%.2f", Double.parseDouble(amountObj.toString())) : "0.00";

            NotificationRequest request = new NotificationRequest();
            request.setUserId(studentId);
            request.setUserEmail(studentEmail);
            request.setSendEmail(studentEmail != null && !studentEmail.isBlank());

            if ("COURSE_PURCHASE".equalsIgnoreCase(paymentType)) {
                // ── Course purchase notification ──────────────────────────
                String courseTitle = (String) event.get("courseTitle");
                Long   courseId    = toLong(event.get("courseId"));
                String txnId       = (String) event.get("transactionId");

                request.setType("PAYMENT_SUCCESS");
                request.setTitle("Payment Successful!");
                request.setMessage(
                        "Your payment of ₹" + amount + " for \"" + courseTitle + "\" was successful. " +
                                "Transaction ID: " + (txnId != null ? txnId : "N/A") + ". " +
                                "You can now access all course content from your dashboard."
                );
                request.setRelatedEntityId(courseId);
                request.setRelatedEntityType("COURSE");

            } else if ("SUBSCRIPTION".equalsIgnoreCase(paymentType)) {
                // ── Subscription activation notification ──────────────────
                String plan = (String) event.get("subscriptionPlan");

                request.setType("PAYMENT_SUCCESS");
                request.setTitle("Subscription Activated!");
                request.setMessage(
                        "Your " + plan + " subscription has been activated for ₹" + amount + ". " +
                                "You now have full access to all courses on the Learnify platform."
                );
                request.setRelatedEntityType("SUBSCRIPTION");

            } else {
                log.warn("[PaymentListener] Unknown payment type: {}", paymentType);
                return;
            }

            notificationService.sendNotification(request);

            log.info("[PaymentListener] Notification sent — student={} type={}",
                    studentId, paymentType);

        } catch (Exception e) {
            log.error("[PaymentListener] Failed to process PAYMENT_SUCCESS event: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}
