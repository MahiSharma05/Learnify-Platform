package com.learnify.paymentservice.repository;

import com.learnify.paymentservice.entity.Subscription;
import com.learnify.paymentservice.enums.SubscriptionPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================
 * SUBSCRIPTION REPOSITORY TEST
 * =========================================================
 */
@DataJpaTest
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Helper method:
     * Creates sample subscription.
     */
    private Subscription createSubscription() {

        Subscription sub = new Subscription();

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
    // SAVE TEST
    // =========================================================

    @Test
    @DisplayName("Should save subscription")
    void shouldSaveSubscription() {

        // Arrange
        Subscription sub = createSubscription();

        // Act
        Subscription saved =
                subscriptionRepository.save(sub);

        // Assert
        assertNotNull(saved);

        assertNotNull(saved.getId());

        assertEquals(
                SubscriptionPlan.MONTHLY,
                saved.getPlan()
        );
    }

    // =========================================================
    // FIND ACTIVE SUBSCRIPTION TEST
    // =========================================================

    @Test
    @DisplayName("Should find active subscription")
    void shouldFindActiveSubscription() {

        // Arrange
        Subscription sub = createSubscription();

        subscriptionRepository.save(sub);

        // Act
        Optional<Subscription> result =
                subscriptionRepository
                        .findByStudentIdAndStatus(
                                101L,
                                "ACTIVE"
                        );

        // Assert
        assertTrue(result.isPresent());

        assertEquals(
                "ACTIVE",
                result.get().getStatus()
        );
    }

    // =========================================================
    // FIND HISTORY TEST
    // =========================================================

    @Test
    @DisplayName("Should find subscription history")
    void shouldFindSubscriptionHistory() {

        // Arrange
        Subscription sub = createSubscription();

        subscriptionRepository.save(sub);

        // Act
        List<Subscription> list =
                subscriptionRepository
                        .findByStudentIdOrderByCreatedAtDesc(101L);

        // Assert
        assertEquals(1, list.size());

        assertEquals(
                101L,
                list.get(0).getStudentId()
        );
    }

    // =========================================================
    // FIND LATEST SUBSCRIPTION TEST
    // =========================================================

    @Test
    @DisplayName("Should find latest subscription")
    void shouldFindLatestSubscription() {

        // Arrange
        Subscription sub = createSubscription();

        subscriptionRepository.save(sub);

        // Act
        Optional<Subscription> result =
                subscriptionRepository
                        .findTopByStudentIdOrderByCreatedAtDesc(101L);

        // Assert
        assertTrue(result.isPresent());
    }

    // =========================================================
    // COUNT BY PLAN TEST
    // =========================================================

    @Test
    @DisplayName("Should count subscriptions by plan")
    void shouldCountSubscriptionsByPlan() {

        // Arrange
        Subscription sub = createSubscription();

        subscriptionRepository.save(sub);

        // Act
        long count =
                subscriptionRepository.countByPlan(
                        SubscriptionPlan.MONTHLY
                );

        // Assert
        assertEquals(1, count);
    }

    // =========================================================
    // COUNT ACTIVE TEST
    // =========================================================

    @Test
    @DisplayName("Should count active subscriptions")
    void shouldCountActiveSubscriptions() {

        // Arrange
        Subscription sub = createSubscription();

        subscriptionRepository.save(sub);

        // Act
        long count =
                subscriptionRepository.countByStatus("ACTIVE");

        // Assert
        assertEquals(1, count);
    }

    // =========================================================
    // EXISTS TEST
    // =========================================================

    @Test
    @DisplayName("Should check active subscription exists")
    void shouldCheckActiveSubscriptionExists() {

        // Arrange
        Subscription sub = createSubscription();

        subscriptionRepository.save(sub);

        // Act
        boolean exists =
                subscriptionRepository
                        .existsByStudentIdAndStatus(
                                101L,
                                "ACTIVE"
                        );

        // Assert
        assertTrue(exists);
    }

    // =========================================================
    // FIND EXPIRING SUBSCRIPTIONS TEST
    // =========================================================

    @Test
    @DisplayName("Should find expiring subscriptions")
    void shouldFindExpiringSubscriptions() {

        // Arrange
        Subscription sub = createSubscription();

        sub.setEndDate(LocalDate.now().minusDays(1));

        subscriptionRepository.save(sub);

        // Act
        List<Subscription> result =
                subscriptionRepository
                        .findByEndDateBeforeAndStatus(
                                LocalDate.now(),
                                "ACTIVE"
                        );

        // Assert
        assertEquals(1, result.size());
    }

    // =========================================================
    // EMPTY RESULT TEST
    // =========================================================

    @Test
    @DisplayName("Should return empty when no subscription found")
    void shouldReturnEmptyWhenNoSubscriptionFound() {

        // Act
        Optional<Subscription> result =
                subscriptionRepository
                        .findByStudentIdAndStatus(
                                999L,
                                "ACTIVE"
                        );

        // Assert
        assertFalse(result.isPresent());
    }
}