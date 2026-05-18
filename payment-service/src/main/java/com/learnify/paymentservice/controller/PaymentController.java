package com.learnify.paymentservice.controller;
import com.learnify.paymentservice.dto.*;
import com.learnify.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ================= RAZORPAY APIs =================

    /**
     * STEP 1: Create Razorpay Order
     */
    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @Valid @RequestBody RazorpayOrderRequest request,
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        if (!"STUDENT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                paymentService.createRazorpayOrder(request, studentId, email)
        );
    }


    /**
     * STEP 2: Verify Payment
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        if (!"STUDENT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(201).body(
                paymentService.verifyAndSavePayment(request, studentId, email)
        );
    }
    /**
     * POST /api/payments
     * Process a one-time course purchase.
     * Roles: STUDENT
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        if (!"STUDENT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(201)
                .body(paymentService.processPayment(request, studentId, email));
    }

    /**
     * GET /api/payments/my
     * Get payment history for the logged-in student.
     * Roles: STUDENT
     */
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }

    /**
     * GET /api/payments/{id}
     * Get a specific payment by ID.
     * Roles: STUDENT (own), ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   Long studentId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(paymentService.getPaymentById(id, studentId, role));
    }

    /**
     * GET /api/payments/course/{courseId}
     * Get all payments for a course.
     * Roles: ADMIN
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(paymentService.getPaymentsByCourse(courseId, role));
    }

    /**
     * POST /api/payments/{id}/refund
     * Refund a payment.
     * Roles: ADMIN only
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(paymentService.refundPayment(id, role));
    }

    /**
     * GET /api/payments/check/{courseId}
     * Check if the logged-in student has paid for a course.
     * Roles: STUDENT
     * Returns: { "paid": true/false }
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<Map<String, Boolean>> hasPaid(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long studentId) {

        boolean paid = paymentService.hasPaidForCourse(studentId, courseId);
        return ResponseEntity.ok(Map.of("paid", paid));
    }

    /**
     * GET /api/payments/revenue
     * Get platform revenue summary.
     * Roles: ADMIN
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueSummary(
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(paymentService.getRevenueSummary(role));
    }
    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
}