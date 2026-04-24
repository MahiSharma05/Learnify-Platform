package com.learnify.paymentservice.repository;

import com.learnify.paymentservice.entity.Payment;
import com.learnify.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // All payments made by a student (payment history)
    List<Payment> findByStudentIdOrderByPaidAtDesc(Long studentId);

    // All payments for a specific course (admin analytics)
    List<Payment> findByCourseId(Long courseId);

    // Find by transaction ID (for refund lookup / duplicate check)
    Optional<Payment> findByTransactionId(String transactionId);

    // Filter by status
    List<Payment> findByStatus(PaymentStatus status);

    // All payments by a student for a specific course
    List<Payment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Check if student has a successful payment for a course
    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId,
                                                  PaymentStatus status);

    // Total revenue across all successful payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double getTotalRevenue();

    // Total revenue for a specific course
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.courseId = :courseId AND p.status = 'SUCCESS'")
    Double getRevenueByCourse(@Param("courseId") Long courseId);

    // Total spent by a student
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.studentId = :studentId AND p.status = 'SUCCESS'")
    Double getTotalSpentByStudent(@Param("studentId") Long studentId);
}