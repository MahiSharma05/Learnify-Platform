package com.learnify.paymentservice.service;

import com.learnify.paymentservice.dto.*;
import com.learnify.paymentservice.entity.Payment;
import com.learnify.paymentservice.entity.Subscription;
import com.learnify.paymentservice.enums.PaymentMode;
import com.learnify.paymentservice.enums.PaymentStatus;
import com.learnify.paymentservice.enums.SubscriptionPlan;
import com.learnify.paymentservice.exception.PaymentException;
import com.learnify.paymentservice.exception.ResourceNotFoundException;
import com.learnify.paymentservice.exception.UnauthorizedException;
import com.learnify.paymentservice.repository.PaymentRepository;
import com.learnify.paymentservice.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    // Subscription plan pricing (INR) — configurable via DB/config in production
    private static final Map<SubscriptionPlan, Double> PLAN_PRICES = Map.of(
            SubscriptionPlan.FREE,    0.0,
            SubscriptionPlan.MONTHLY, 499.0,
            SubscriptionPlan.ANNUAL,  3999.0
    );

    // Plan durations in days
    private static final Map<SubscriptionPlan, Integer> PLAN_DAYS = Map.of(
            SubscriptionPlan.FREE,    36500, // FREE = effectively no expiry (100 years)
            SubscriptionPlan.MONTHLY, 30,
            SubscriptionPlan.ANNUAL,  365
    );

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              SubscriptionRepository subscriptionRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // ── Payment Operations ────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request,
                                          Long studentId, String studentEmail) {

        // 🔒 Prevent duplicate transaction
        if (request.getTransactionId() != null) {
            paymentRepository.findByTransactionId(request.getTransactionId())
                    .ifPresent(p -> {
                        throw new PaymentException(
                                "Duplicate transaction ID: " + request.getTransactionId());
                    });
        }

        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setStudentEmail(studentEmail);
        payment.setCourseId(request.getCourseId());
        payment.setCourseTitle(request.getCourseTitle());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        payment.setMode(PaymentMode.valueOf(request.getMode().toUpperCase()));

        // Generate transaction ID if not provided
        String txnId = request.getTransactionId() != null
                ? request.getTransactionId()
                : generateTransactionId();
        payment.setTransactionId(txnId);

        /**
         * In production: call payment gateway (Razorpay / Stripe) here.
         * For simulation: mark all payments as SUCCESS immediately.
         */
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment saved = paymentRepository.save(payment);
        return mapToPaymentResponse(saved);
    }

    @Override
    public List<PaymentResponse> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentIdOrderByPaidAtDesc(studentId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByCourse(Long courseId, String userRole) {
        if (!isAdmin(userRole)) {
            throw new UnauthorizedException("Only admins can view all course payments");
        }
        return paymentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId, Long studentId, String userRole) {

        Payment payment = findPaymentOrThrow(paymentId);

        // 🔒 Student can only view their own payments
        if (!payment.getStudentId().equals(studentId) && !isAdmin(userRole)) {
            throw new UnauthorizedException("Access denied for this payment record");
        }

        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String userRole) {

        if (!isAdmin(userRole)) {
            throw new UnauthorizedException("Only admins can process refunds");
        }

        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentException("Payment has already been refunded");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException(
                    "Only successful payments can be refunded. Current status: "
                            + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());

        return mapToPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public boolean hasPaidForCourse(Long studentId, Long courseId) {
        return paymentRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId, courseId, PaymentStatus.SUCCESS);
    }

    @Override
    public Map<String, Object> getRevenueSummary(String userRole) {

        if (!isAdmin(userRole)) {
            throw new UnauthorizedException("Only admins can view revenue summary");
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRevenue",       paymentRepository.getTotalRevenue());
        summary.put("totalPayments",      paymentRepository.count());
        summary.put("activeSubscriptions",
                subscriptionRepository.countByStatus("ACTIVE"));
        summary.put("monthlySubscribers",
                subscriptionRepository.countByPlan(SubscriptionPlan.MONTHLY));
        summary.put("annualSubscribers",
                subscriptionRepository.countByPlan(SubscriptionPlan.ANNUAL));
        summary.put("freeSubscribers",
                subscriptionRepository.countByPlan(SubscriptionPlan.FREE));

        return summary;
    }

    // ── Subscription Operations ───────────────────────────────────────────────

    @Override
    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request,
                                          Long studentId, String studentEmail) {

        SubscriptionPlan plan = parsePlan(request.getPlan());

        // 🔒 Cancel existing active subscription before creating new one
        subscriptionRepository.findByStudentIdAndStatus(studentId, "ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("CANCELLED");
                    existing.setCancelledAt(LocalDateTime.now());
                    subscriptionRepository.save(existing);
                });

        // Create payment record for paid plans
        Long paymentId = null;
        if (plan != SubscriptionPlan.FREE) {

            Payment payment = new Payment();
            payment.setStudentId(studentId);
            payment.setStudentEmail(studentEmail);
            payment.setAmount(PLAN_PRICES.get(plan));
            payment.setCurrency("INR");
            payment.setMode(request.getPaymentMode() != null
                    ? PaymentMode.valueOf(request.getPaymentMode().toUpperCase())
                    : PaymentMode.CARD);
            payment.setTransactionId(request.getTransactionId() != null
                    ? request.getTransactionId()
                    : generateTransactionId());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCourseTitle("Subscription: " + plan.name());

            Payment savedPayment = paymentRepository.save(payment);
            paymentId = savedPayment.getId();
        }

        // Create subscription record
        LocalDate start = LocalDate.now();
        LocalDate end   = start.plusDays(PLAN_DAYS.get(plan));

        Subscription subscription = new Subscription();
        subscription.setStudentId(studentId);
        subscription.setStudentEmail(studentEmail);
        subscription.setPlan(plan);
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.setStatus("ACTIVE");
        subscription.setAmountPaid(PLAN_PRICES.get(plan));
        subscription.setAutoRenew(request.isAutoRenew());
        subscription.setPaymentId(paymentId);

        return mapToSubscriptionResponse(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long subscriptionId,
                                                   Long studentId, String userRole) {

        Subscription subscription = findSubscriptionOrThrow(subscriptionId);

        // 🔒 Only the student or ADMIN can cancel
        if (!subscription.getStudentId().equals(studentId) && !isAdmin(userRole)) {
            throw new UnauthorizedException(
                    "You are not allowed to cancel this subscription");
        }

        if ("CANCELLED".equals(subscription.getStatus())) {
            throw new PaymentException("Subscription is already cancelled");
        }

        subscription.setStatus("CANCELLED");
        subscription.setAutoRenew(false);
        subscription.setCancelledAt(LocalDateTime.now());

        return mapToSubscriptionResponse(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(Long subscriptionId,
                                                  Long studentId, String userRole) {

        Subscription subscription = findSubscriptionOrThrow(subscriptionId);

        // 🔒 Only the student or ADMIN can renew
        if (!subscription.getStudentId().equals(studentId) && !isAdmin(userRole)) {
            throw new UnauthorizedException(
                    "You are not allowed to renew this subscription");
        }

        SubscriptionPlan plan = subscription.getPlan();

        // Extend from current endDate (or today if already expired)
        LocalDate base = subscription.getEndDate().isAfter(LocalDate.now())
                ? subscription.getEndDate()
                : LocalDate.now();

        subscription.setEndDate(base.plusDays(PLAN_DAYS.get(plan)));
        subscription.setStatus("ACTIVE");
        subscription.setAmountPaid(PLAN_PRICES.get(plan));

        return mapToSubscriptionResponse(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponse getSubscriptionByStudent(Long studentId) {

        Subscription subscription = subscriptionRepository
                .findByStudentIdAndStatus(studentId, "ACTIVE")
                .orElseGet(() ->
                        subscriptionRepository
                                .findTopByStudentIdOrderByCreatedAtDesc(studentId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "No subscription found for student: " + studentId))
                );

        return mapToSubscriptionResponse(subscription);
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionHistory(Long studentId) {
        return subscriptionRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::mapToSubscriptionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSubscriptionActive(Long studentId) {
        return subscriptionRepository
                .findByStudentIdAndStatus(studentId, "ACTIVE")
                .map(sub -> !sub.getEndDate().isBefore(LocalDate.now()))
                .orElse(false);
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptions(String userRole) {
        if (!isAdmin(userRole)) {
            throw new UnauthorizedException("Only admins can view all subscriptions");
        }
        return subscriptionRepository.findAll()
                .stream()
                .map(this::mapToSubscriptionResponse)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Payment findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + id));
    }

    private Subscription findSubscriptionOrThrow(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + id));
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private SubscriptionPlan parsePlan(String plan) {
        try {
            return SubscriptionPlan.valueOf(plan.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PaymentException(
                    "Invalid plan: " + plan + ". Must be FREE, MONTHLY, or ANNUAL");
        }
    }

    /**
     * Generate a mock transaction ID.
     * In production replace with actual gateway response ID.
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    // Map Payment entity → PaymentResponse DTO
    private PaymentResponse mapToPaymentResponse(Payment p) {
        PaymentResponse response = new PaymentResponse();
        response.setId(p.getId());
        response.setStudentId(p.getStudentId());
        response.setStudentEmail(p.getStudentEmail());
        response.setCourseId(p.getCourseId());
        response.setCourseTitle(p.getCourseTitle());
        response.setAmount(p.getAmount());
        response.setCurrency(p.getCurrency());
        response.setMode(p.getMode().name());
        response.setStatus(p.getStatus().name());
        response.setTransactionId(p.getTransactionId());
        response.setFailureReason(p.getFailureReason());
        response.setPaidAt(p.getPaidAt());
        response.setRefundedAt(p.getRefundedAt());
        return response;
    }

    // Map Subscription entity → SubscriptionResponse DTO
    private SubscriptionResponse mapToSubscriptionResponse(Subscription s) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(s.getId());
        response.setStudentId(s.getStudentId());
        response.setStudentEmail(s.getStudentEmail());
        response.setPlan(s.getPlan().name());
        response.setStartDate(s.getStartDate());
        response.setEndDate(s.getEndDate());
        response.setStatus(s.getStatus());
        response.setAmountPaid(s.getAmountPaid());
        response.setAutoRenew(s.isAutoRenew());
        response.setPaymentId(s.getPaymentId());
        response.setCreatedAt(s.getCreatedAt());
        response.setCancelledAt(s.getCancelledAt());

        // Computed: is subscription currently active and not expired?
        boolean active = "ACTIVE".equals(s.getStatus())
                && !s.getEndDate().isBefore(LocalDate.now());
        response.setActive(active);

        // Computed: days remaining
        long daysRemaining = active
                ? ChronoUnit.DAYS.between(LocalDate.now(), s.getEndDate())
                : 0;
        response.setDaysRemaining(daysRemaining);

        return response;
    }
}