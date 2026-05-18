package com.learnify.paymentservice.controller;

import com.learnify.paymentservice.dto.SubscriptionRequest;
import com.learnify.paymentservice.dto.SubscriptionResponse;
import com.learnify.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final PaymentService paymentService;

    public SubscriptionController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/subscriptions
     * Subscribe to a plan (FREE / MONTHLY / ANNUAL).
     * Roles: STUDENT
     */
    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscriptionRequest request,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        if (!"STUDENT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(201)
                .body(paymentService.subscribe(request, studentId, email));
    }

    /**
     * GET /api/subscriptions/my
     * Get the current subscription for the logged-in student.
     * Roles: STUDENT
     */
    @GetMapping("/my")
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(paymentService.getSubscriptionByStudent(studentId));
    }

    /**
     * GET /api/subscriptions/my/history
     * Get full subscription history for the logged-in student.
     * Roles: STUDENT
     */
    @GetMapping("/my/history")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionHistory(
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(paymentService.getSubscriptionHistory(studentId));
    }

    /**
     * GET /api/subscriptions/active
     * Check if the logged-in student has an active subscription.
     * Roles: STUDENT
     * Returns: { "active": true/false }
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Boolean>> isActive(
            @RequestHeader("X-User-Id") Long studentId) {

        boolean active = paymentService.isSubscriptionActive(studentId);
        return ResponseEntity.ok(Map.of("active", active));
    }

    /**
     * DELETE /api/subscriptions/{id}
     * Cancel a subscription.
     * Roles: STUDENT (own), ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   Long studentId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
                paymentService.cancelSubscription(id, studentId, role));
    }

    /**
     * PUT /api/subscriptions/{id}/renew
     * Renew a subscription.
     * Roles: STUDENT (own), ADMIN
     */
    @PutMapping("/{id}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   Long studentId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
                paymentService.renewSubscription(id, studentId, role));
    }

    /**
     * GET /api/subscriptions/all
     * Get all subscriptions platform-wide.
     * Roles: ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions(
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(paymentService.getAllSubscriptions(role));
    }
}