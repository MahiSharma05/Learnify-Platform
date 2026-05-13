package com.learnify.paymentservice.service;

import com.learnify.paymentservice.dto.*;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    // ─────────────────────────────────────────────────────────────
    // ✅ NEW: Razorpay Integration Methods
    // ─────────────────────────────────────────────────────────────

    /**
     * STEP 1:
     * Create Razorpay order before opening payment UI
     */
    RazorpayOrderResponse createRazorpayOrder(
            RazorpayOrderRequest request,
            Long studentId,
            String studentEmail
    );

    /**
     * STEP 2:
     * Verify Razorpay payment after success
     */
    PaymentResponse verifyAndSavePayment(
            PaymentVerifyRequest request,
            Long studentId,
            String studentEmail
    );

    // ─────────────────────────────────────────────────────────────
    // ── Payment Operations (EXISTING — NO CHANGE)
    // ─────────────────────────────────────────────────────────────

    // Process a one-time course purchase (STUDENT)
    PaymentResponse processPayment(PaymentRequest request, Long studentId, String studentEmail);

    // Get payment history for the logged-in student
    List<PaymentResponse> getPaymentsByStudent(Long studentId);

    // Get all payments for a course (ADMIN analytics)
    List<PaymentResponse> getPaymentsByCourse(Long courseId, String userRole);

    // Get a single payment by ID
    PaymentResponse getPaymentById(Long paymentId, Long studentId, String userRole);

    // Refund a payment (ADMIN only)
    PaymentResponse refundPayment(Long paymentId, String userRole);

    // Check if student has paid for a specific course
    boolean hasPaidForCourse(Long studentId, Long courseId);

    // Get platform revenue summary (ADMIN)
    Map<String, Object> getRevenueSummary(String userRole);

    // ─────────────────────────────────────────────────────────────
    // ── Subscription Operations (NO CHANGE)
    // ─────────────────────────────────────────────────────────────

    // Subscribe to a plan (STUDENT)
    SubscriptionResponse subscribe(SubscriptionRequest request, Long studentId,
                                   String studentEmail);

    // Cancel active subscription (STUDENT own, ADMIN any)
    SubscriptionResponse cancelSubscription(Long subscriptionId, Long studentId,
                                            String userRole);

    // Renew a subscription (STUDENT own, ADMIN any)
    SubscriptionResponse renewSubscription(Long subscriptionId, Long studentId,
                                           String userRole);

    // Get the current subscription for a student
    SubscriptionResponse getSubscriptionByStudent(Long studentId);

    // Get full subscription history for a student
    List<SubscriptionResponse> getSubscriptionHistory(Long studentId);

    // Check if student has an active subscription
    boolean isSubscriptionActive(Long studentId);

    // Get all subscriptions platform-wide (ADMIN)
    List<SubscriptionResponse> getAllSubscriptions(String userRole);
    List<PaymentResponse> getAllPayments();
}