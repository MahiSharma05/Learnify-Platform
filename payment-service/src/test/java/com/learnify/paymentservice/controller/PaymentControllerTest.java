package com.learnify.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.paymentservice.dto.*;
import com.learnify.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(PaymentController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    // =========================================================
    // TEST DATA METHODS
    // =========================================================

    private PaymentResponse getPaymentResponse() {

        PaymentResponse response = new PaymentResponse();

        response.setId(1L);
        response.setStudentId(101L);
        response.setStudentEmail("student@test.com");

        response.setCourseId(10L);
        response.setCourseTitle("Spring Boot");

        response.setAmount(999.0);
        response.setCurrency("INR");

        response.setMode("CARD");
        response.setStatus("SUCCESS");

        response.setTransactionId("TXN123456");

        response.setPaidAt(LocalDateTime.now());

        return response;
    }

    private RazorpayOrderResponse getOrderResponse() {

        return RazorpayOrderResponse.builder()
                .orderId("order_123")
                .amount(99900L)
                .currency("INR")
                .keyId("rzp_test_123")
                .courseId(10L)
                .courseTitle("Spring Boot")
                .build();
    }

    // =========================================================
    // CREATE ORDER TESTS
    // =========================================================

    @Test
    @DisplayName("Should create Razorpay order successfully")
    void shouldCreateRazorpayOrder() throws Exception {

        RazorpayOrderRequest request = new RazorpayOrderRequest();
        request.setCourseId(10L);
        request.setCourseTitle("Spring Boot");
        request.setAmount(999.0);

        when(paymentService.createRazorpayOrder(any(), anyLong(), anyString()))
                .thenReturn(getOrderResponse());

        mockMvc.perform(post("/api/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 101)
                        .header("X-User-Email", "student@test.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_123"))
                .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    @DisplayName("Should return 403 when role is not STUDENT")
    void shouldReturn403WhenRoleIsNotStudent() throws Exception {

        RazorpayOrderRequest request = new RazorpayOrderRequest();
        request.setCourseId(10L);
        request.setAmount(999.0);

        mockMvc.perform(post("/api/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 101)
                        .header("X-User-Email", "admin@test.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {

        RazorpayOrderRequest request = new RazorpayOrderRequest();

        mockMvc.perform(post("/api/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 101)
                        .header("X-User-Email", "student@test.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // VERIFY PAYMENT TEST
    // =========================================================

    @Test
    @DisplayName("Should verify payment successfully")
    void shouldVerifyPaymentSuccessfully() throws Exception {

        PaymentVerifyRequest request = new PaymentVerifyRequest();

        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("signature");
        request.setCourseId(10L);
        request.setAmount(999.0);

        when(paymentService.verifyAndSavePayment(any(), anyLong(), anyString()))
                .thenReturn(getPaymentResponse());

        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 101)
                        .header("X-User-Email", "student@test.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    // =========================================================
    // PROCESS PAYMENT TEST
    // =========================================================

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() throws Exception {

        PaymentRequest request = new PaymentRequest();

        request.setCourseId(10L);
        request.setCourseTitle("Spring Boot");
        request.setAmount(999.0);
        request.setMode("CARD");

        when(paymentService.processPayment(any(), anyLong(), anyString()))
                .thenReturn(getPaymentResponse());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 101)
                        .header("X-User-Email", "student@test.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(999.0));
    }

    // =========================================================
    // GET MY PAYMENTS TEST
    // =========================================================

    @Test
    @DisplayName("Should return payment history")
    void shouldReturnPaymentHistory() throws Exception {

        when(paymentService.getPaymentsByStudent(101L))
                .thenReturn(List.of(getPaymentResponse()));

        mockMvc.perform(get("/api/payments/my")
                        .header("X-User-Id", 101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(101));
    }

    // =========================================================
    // GET PAYMENT BY ID TEST
    // =========================================================

    @Test
    @DisplayName("Should return payment by id")
    void shouldReturnPaymentById() throws Exception {

        when(paymentService.getPaymentById(1L, 101L, "STUDENT"))
                .thenReturn(getPaymentResponse());

        mockMvc.perform(get("/api/payments/1")
                        .header("X-User-Id", 101)
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // =========================================================
    // REFUND PAYMENT TEST
    // =========================================================

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPayment() throws Exception {

        PaymentResponse response = getPaymentResponse();
        response.setStatus("REFUNDED");

        when(paymentService.refundPayment(1L, "ADMIN"))
                .thenReturn(response);

        mockMvc.perform(post("/api/payments/1/refund")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    // =========================================================
    // CHECK PAYMENT TEST
    // =========================================================

    @Test
    @DisplayName("Should return paid true")
    void shouldReturnPaidTrue() throws Exception {

        when(paymentService.hasPaidForCourse(101L, 10L))
                .thenReturn(true);

        mockMvc.perform(get("/api/payments/check/10")
                        .header("X-User-Id", 101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));
    }

    // =========================================================
    // REVENUE SUMMARY TEST
    // =========================================================

    @Test
    @DisplayName("Should return revenue summary")
    void shouldReturnRevenueSummary() throws Exception {

        Map<String, Object> summary = Map.of(
                "totalRevenue", 5000,
                "totalPayments", 10
        );

        when(paymentService.getRevenueSummary("ADMIN"))
                .thenReturn(summary);

        mockMvc.perform(get("/api/payments/revenue")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(5000));
    }

    // =========================================================
    // GET ALL PAYMENTS TEST
    // =========================================================

    @Test
    @DisplayName("Should return all payments for admin")
    void shouldReturnAllPaymentsForAdmin() throws Exception {

        when(paymentService.getAllPayments())
                .thenReturn(List.of(getPaymentResponse()));

        mockMvc.perform(get("/api/payments/all")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("Should return 403 for non admin")
    void shouldReturn403ForNonAdmin() throws Exception {

        mockMvc.perform(get("/api/payments/all")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }
}
