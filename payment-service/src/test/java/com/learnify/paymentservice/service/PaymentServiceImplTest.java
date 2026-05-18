package com.learnify.paymentservice.service;

import com.learnify.paymentservice.dto.PaymentRequest;
import com.learnify.paymentservice.dto.PaymentResponse;
import com.learnify.paymentservice.dto.SubscriptionRequest;
import com.learnify.paymentservice.dto.SubscriptionResponse;
import com.learnify.paymentservice.entity.Payment;
import com.learnify.paymentservice.entity.Subscription;
import com.learnify.paymentservice.enums.PaymentMode;
import com.learnify.paymentservice.enums.PaymentStatus;
import com.learnify.paymentservice.enums.SubscriptionPlan;
import com.learnify.paymentservice.event.PaymentEventPublisher;
import com.learnify.paymentservice.exception.PaymentException;
import com.learnify.paymentservice.exception.ResourceNotFoundException;
import com.learnify.paymentservice.exception.UnauthorizedException;
import com.learnify.paymentservice.repository.PaymentRepository;
import com.learnify.paymentservice.repository.SubscriptionRepository;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @Mock
    private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(
                paymentService,
                "razorpayKeySecret",
                "test_secret"
        );

        ReflectionTestUtils.setField(
                paymentService,
                "razorpayKeyId",
                "test_key"
        );
    }

    // =========================================================
    // TEST DATA METHODS
    // =========================================================

    private Payment getPayment() {

        Payment payment = new Payment();

        payment.setId(1L);
        payment.setStudentId(101L);
        payment.setStudentEmail("student@test.com");

        payment.setCourseId(10L);
        payment.setCourseTitle("Spring Boot");

        payment.setAmount(999.0);
        payment.setCurrency("INR");

        payment.setMode(PaymentMode.CARD);
        payment.setStatus(PaymentStatus.SUCCESS);

        payment.setTransactionId("TXN123");

        payment.setPaidAt(LocalDateTime.now());

        return payment;
    }

    private Subscription getSubscription() {

        Subscription sub = new Subscription();

        sub.setId(1L);
        sub.setStudentId(101L);
        sub.setStudentEmail("student@test.com");

        sub.setPlan(SubscriptionPlan.MONTHLY);

        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusDays(30));

        sub.setStatus("ACTIVE");

        sub.setAmountPaid(499.0);

        sub.setAutoRenew(true);

        return sub;
    }

    // =========================================================
    // PROCESS PAYMENT TESTS
    // =========================================================

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() {

        PaymentRequest request = new PaymentRequest();

        request.setCourseId(10L);
        request.setCourseTitle("Spring Boot");
        request.setAmount(999.0);
        request.setMode("CARD");

        Payment payment = getPayment();

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        PaymentResponse response = paymentService.processPayment(
                request,
                101L,
                "student@test.com"
        );

        assertNotNull(response);

        assertEquals("SUCCESS", response.getStatus());

        verify(paymentRepository, times(1))
                .save(any(Payment.class));

        verify(paymentEventPublisher, times(1))
                .publishPaymentSuccess(any());
    }

    @Test
    @DisplayName("Should throw exception for duplicate transaction")
    void shouldThrowExceptionForDuplicateTransaction() {

        PaymentRequest request = new PaymentRequest();

        request.setCourseId(10L);
        request.setAmount(999.0);
        request.setMode("CARD");
        request.setTransactionId("TXN123");

        when(paymentRepository.findByTransactionId("TXN123"))
                .thenReturn(Optional.of(getPayment()));

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(
                        request,
                        101L,
                        "student@test.com"
                )
        );
    }

    // =========================================================
    // GET PAYMENTS TEST
    // =========================================================

    @Test
    @DisplayName("Should get payments by student")
    void shouldGetPaymentsByStudent() {

        when(paymentRepository.findByStudentIdOrderByPaidAtDesc(101L))
                .thenReturn(List.of(getPayment()));

        List<PaymentResponse> responses =
                paymentService.getPaymentsByStudent(101L);

        assertEquals(1, responses.size());

        assertEquals(
                "student@test.com",
                responses.get(0).getStudentEmail()
        );
    }

    // =========================================================
    // GET PAYMENT BY ID TESTS
    // =========================================================

    @Test
    @DisplayName("Should return payment by id")
    void shouldReturnPaymentById() {

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(getPayment()));

        PaymentResponse response =
                paymentService.getPaymentById(
                        1L,
                        101L,
                        "STUDENT"
                );

        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should throw unauthorized exception")
    void shouldThrowUnauthorizedException() {

        Payment payment = getPayment();

        payment.setStudentId(999L);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        assertThrows(
                UnauthorizedException.class,
                () -> paymentService.getPaymentById(
                        1L,
                        101L,
                        "STUDENT"
                )
        );
    }

    @Test
    @DisplayName("Should throw resource not found")
    void shouldThrowResourceNotFound() {

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentById(
                        1L,
                        101L,
                        "ADMIN"
                )
        );
    }

    // =========================================================
    // REFUND PAYMENT TESTS
    // =========================================================

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() {

        Payment payment = getPayment();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        PaymentResponse response =
                paymentService.refundPayment(1L, "ADMIN");

        assertEquals("REFUNDED", response.getStatus());

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject already refunded payment")
    void shouldRejectAlreadyRefundedPayment() {

        Payment payment = getPayment();

        payment.setStatus(PaymentStatus.REFUNDED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        assertThrows(
                PaymentException.class,
                () -> paymentService.refundPayment(1L, "ADMIN")
        );
    }

    @Test
    @DisplayName("Should reject refund for non admin")
    void shouldRejectRefundForNonAdmin() {

        assertThrows(
                UnauthorizedException.class,
                () -> paymentService.refundPayment(1L, "STUDENT")
        );
    }

    // =========================================================
    // HAS PAID TEST
    // =========================================================

    @Test
    @DisplayName("Should return true when student paid")
    void shouldReturnTrueWhenStudentPaid() {

        when(paymentRepository.existsByStudentIdAndCourseIdAndStatus(
                101L,
                10L,
                PaymentStatus.SUCCESS
        )).thenReturn(true);

        boolean result =
                paymentService.hasPaidForCourse(101L, 10L);

        assertTrue(result);
    }

    // =========================================================
    // REVENUE SUMMARY TEST
    // =========================================================

    @Test
    @DisplayName("Should return revenue summary")
    void shouldReturnRevenueSummary() {

        when(paymentRepository.getTotalRevenue())
                .thenReturn(5000.0);

        when(paymentRepository.count())
                .thenReturn(10L);

        when(subscriptionRepository.countByStatus("ACTIVE"))
                .thenReturn(5L);

        when(subscriptionRepository.countByPlan(SubscriptionPlan.MONTHLY))
                .thenReturn(2L);

        when(subscriptionRepository.countByPlan(SubscriptionPlan.ANNUAL))
                .thenReturn(2L);

        when(subscriptionRepository.countByPlan(SubscriptionPlan.FREE))
                .thenReturn(1L);

        Map<String, Object> summary =
                paymentService.getRevenueSummary("ADMIN");

        assertEquals(5000.0, summary.get("totalRevenue"));
    }

    // =========================================================
    // SUBSCRIPTION TESTS
    // =========================================================

    @Test
    @DisplayName("Should subscribe successfully")
    void shouldSubscribeSuccessfully() {

        SubscriptionRequest request = new SubscriptionRequest();

        request.setPlan("MONTHLY");
        request.setPaymentMode("CARD");

        when(subscriptionRepository.findByStudentIdAndStatus(
                101L,
                "ACTIVE"
        )).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(getPayment());

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(getSubscription());

        SubscriptionResponse response =
                paymentService.subscribe(
                        request,
                        101L,
                        "student@test.com"
                );

        assertEquals("MONTHLY", response.getPlan());

        verify(paymentEventPublisher)
                .publishPaymentSuccess(any());
    }

    @Test
    @DisplayName("Should cancel subscription")
    void shouldCancelSubscription() {

        Subscription sub = getSubscription();

        when(subscriptionRepository.findById(1L))
                .thenReturn(Optional.of(sub));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(sub);

        SubscriptionResponse response =
                paymentService.cancelSubscription(
                        1L,
                        101L,
                        "STUDENT"
                );

        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    @DisplayName("Should renew subscription")
    void shouldRenewSubscription() {

        Subscription sub = getSubscription();

        when(subscriptionRepository.findById(1L))
                .thenReturn(Optional.of(sub));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(sub);

        SubscriptionResponse response =
                paymentService.renewSubscription(
                        1L,
                        101L,
                        "STUDENT"
                );

        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    @DisplayName("Should return active subscription")
    void shouldReturnActiveSubscription() {

        when(subscriptionRepository.findByStudentIdAndStatus(
                101L,
                "ACTIVE"
        )).thenReturn(Optional.of(getSubscription()));

        boolean active =
                paymentService.isSubscriptionActive(101L);

        assertTrue(active);
    }

    @Test
    @DisplayName("Should throw exception for invalid plan")
    void shouldThrowExceptionForInvalidPlan() {

        SubscriptionRequest request = new SubscriptionRequest();

        request.setPlan("INVALID_PLAN");

        assertThrows(
                PaymentException.class,
                () -> paymentService.subscribe(
                        request,
                        101L,
                        "student@test.com"
                )
        );
    }
}