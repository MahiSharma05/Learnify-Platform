package com.learnify.paymentservice.repository;

import com.learnify.paymentservice.entity.Subscription;
import com.learnify.paymentservice.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Latest active subscription for a student
    Optional<Subscription> findTopByStudentIdOrderByCreatedAtDesc(Long studentId);

    // All subscriptions for a student (history)
    List<Subscription> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    // Active subscription for a student
    Optional<Subscription> findByStudentIdAndStatus(Long studentId, String status);

    // Subscriptions expiring on or before a given date (for auto-renewal jobs)
    List<Subscription> findByEndDateBeforeAndStatus(LocalDate date, String status);

    // Count subscriptions by plan (analytics)
    long countByPlan(SubscriptionPlan plan);

    // Count active subscriptions
    long countByStatus(String status);

    // Check if student has an active subscription
    boolean existsByStudentIdAndStatus(Long studentId, String status);
}