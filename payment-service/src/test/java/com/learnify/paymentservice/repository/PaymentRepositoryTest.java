package com.learnify.paymentservice.repository;

import com.learnify.paymentservice.entity.Payment;
import com.learnify.paymentservice.enums.PaymentMode;
import com.learnify.paymentservice.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================
 * PAYMENT REPOSITORY TEST
 * =========================================================
 *
 * @DataJpaTest:
 *
 * Loads only:
 * - JPA
 * - Hibernate
 * - Repository layer
 * - H2 in-memory database
 *
 * Does NOT load:
 * - Controllers
 * - Services
 * - Security
 *
 * Repository tests are very fast because
 * Spring loads only database-related beans.
 */
@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Helper method:
     * Creates sample payment object.
     *
     * Reusable test data method.
     */
    private Payment createPayment() {

        Payment payment = new Payment();

        // Student info
        payment.setStudentId(101L);
        payment.setStudentEmail("student@test.com");

        // Course info
        payment.setCourseId(10L);
        payment.setCourseTitle("Spring Boot");

        // Payment details
        payment.setAmount(999.0);
        payment.setCurrency("INR");

        payment.setMode(PaymentMode.CARD);

        payment.setStatus(PaymentStatus.SUCCESS);

        payment.setTransactionId("TXN123");

        return payment;
    }

    // =========================================================
    // SAVE PAYMENT TEST
    // =========================================================

    @Test
    @DisplayName("Should save payment successfully")
    void shouldSavePaymentSuccessfully() {

        // Arrange
        Payment payment = createPayment();

        // Act
        Payment savedPayment =
                paymentRepository.save(payment);

        // Assert
        assertNotNull(savedPayment);

        assertNotNull(savedPayment.getId());

        assertEquals(
                "student@test.com",
                savedPayment.getStudentEmail()
        );

        assertEquals(
                999.0,
                savedPayment.getAmount()
        );
    }

    // =========================================================
    // FIND BY ID TEST
    // =========================================================

    @Test
    @DisplayName("Should find payment by id")
    void shouldFindPaymentById() {

        // Arrange
        Payment payment = createPayment();

        Payment savedPayment =
                paymentRepository.save(payment);

        // Act
        Optional<Payment> optionalPayment =
                paymentRepository.findById(savedPayment.getId());

        // Assert
        assertTrue(optionalPayment.isPresent());

        assertEquals(
                "Spring Boot",
                optionalPayment.get().getCourseTitle()
        );
    }

    // =========================================================
    // FIND BY STUDENT TEST
    // =========================================================

    @Test
    @DisplayName("Should find payments by student id")
    void shouldFindPaymentsByStudentId() {

        // Arrange
        Payment payment = createPayment();

        paymentRepository.save(payment);

        // Act
        List<Payment> payments =
                paymentRepository
                        .findByStudentIdOrderByPaidAtDesc(101L);

        // Assert
        assertFalse(payments.isEmpty());

        assertEquals(1, payments.size());

        assertEquals(
                101L,
                payments.get(0).getStudentId()
        );
    }

    // =========================================================
    // FIND BY COURSE TEST
    // =========================================================

    @Test
    @DisplayName("Should find payments by course id")
    void shouldFindPaymentsByCourseId() {

        // Arrange
        Payment payment = createPayment();

        paymentRepository.save(payment);

        // Act
        List<Payment> payments =
                paymentRepository.findByCourseId(10L);

        // Assert
        assertEquals(1, payments.size());

        assertEquals(
                "Spring Boot",
                payments.get(0).getCourseTitle()
        );
    }

    // =========================================================
    // FIND BY TRANSACTION ID TEST
    // =========================================================

    @Test
    @DisplayName("Should find payment by transaction id")
    void shouldFindPaymentByTransactionId() {

        // Arrange
        Payment payment = createPayment();

        paymentRepository.save(payment);

        // Act
        Optional<Payment> optionalPayment =
                paymentRepository.findByTransactionId("TXN123");

        // Assert
        assertTrue(optionalPayment.isPresent());

        assertEquals(
                "TXN123",
                optionalPayment.get().getTransactionId()
        );
    }

    // =========================================================
    // EXISTS TEST
    // =========================================================

    @Test
    @DisplayName("Should check successful payment exists")
    void shouldCheckSuccessfulPaymentExists() {

        // Arrange
        Payment payment = createPayment();

        paymentRepository.save(payment);

        // Act
        boolean exists =
                paymentRepository
                        .existsByStudentIdAndCourseIdAndStatus(
                                101L,
                                10L,
                                PaymentStatus.SUCCESS
                        );

        // Assert
        assertTrue(exists);
    }

    // =========================================================
    // FIND BY STATUS TEST
    // =========================================================

    @Test
    @DisplayName("Should find payments by status")
    void shouldFindPaymentsByStatus() {

        // Arrange
        Payment payment = createPayment();

        paymentRepository.save(payment);

        // Act
        List<Payment> payments =
                paymentRepository.findByStatus(
                        PaymentStatus.SUCCESS
                );

        // Assert
        assertEquals(1, payments.size());

        assertEquals(
                PaymentStatus.SUCCESS,
                payments.get(0).getStatus()
        );
    }

    // =========================================================
    // TOTAL REVENUE TEST
    // =========================================================

    @Test
    @DisplayName("Should calculate total revenue")
    void shouldCalculateTotalRevenue() {

        // Arrange
        Payment payment1 = createPayment();

        Payment payment2 = createPayment();

        payment2.setTransactionId("TXN456");

        paymentRepository.save(payment1);

        paymentRepository.save(payment2);

        // Act
        Double totalRevenue =
                paymentRepository.getTotalRevenue();

        // Assert
        assertEquals(1998.0, totalRevenue);
    }

    // =========================================================
    // REVENUE BY COURSE TEST
    // =========================================================

    @Test
    @DisplayName("Should calculate revenue by course")
    void shouldCalculateRevenueByCourse() {

        // Arrange
        Payment payment1 = createPayment();

        Payment payment2 = createPayment();

        payment2.setTransactionId("TXN999");

        paymentRepository.save(payment1);

        paymentRepository.save(payment2);

        // Act
        Double revenue =
                paymentRepository.getRevenueByCourse(10L);

        // Assert
        assertEquals(1998.0, revenue);
    }

    // =========================================================
    // TOTAL SPENT BY STUDENT TEST
    // =========================================================

    @Test
    @DisplayName("Should calculate total spent by student")
    void shouldCalculateTotalSpentByStudent() {

        // Arrange
        Payment payment1 = createPayment();

        Payment payment2 = createPayment();

        payment2.setTransactionId("TXN888");

        paymentRepository.save(payment1);

        paymentRepository.save(payment2);

        // Act
        Double totalSpent =
                paymentRepository.getTotalSpentByStudent(101L);

        // Assert
        assertEquals(1998.0, totalSpent);
    }

    // =========================================================
    // EMPTY RESULT TEST
    // =========================================================

    @Test
    @DisplayName("Should return empty when transaction not found")
    void shouldReturnEmptyWhenTransactionNotFound() {

        // Act
        Optional<Payment> payment =
                paymentRepository.findByTransactionId("INVALID");

        // Assert
        assertFalse(payment.isPresent());
    }
}