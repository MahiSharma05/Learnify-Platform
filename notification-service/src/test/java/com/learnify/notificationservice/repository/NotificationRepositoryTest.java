package com.learnify.notificationservice.repository;

import com.learnify.notificationservice.entity.Notification;
import com.learnify.notificationservice.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    private Notification notification;

    @BeforeEach
    void setUp() {

        notification = new Notification();
        notification.setUserId(1L);
        notification.setUserEmail("test@gmail.com");
        notification.setType(NotificationType.PAYMENT_SUCCESS);
        notification.setTitle("Test");
        notification.setMessage("Message");
        notification.setRead(false);
        notification.setRelatedEntityId(10L);
        notification.setRelatedEntityType("COURSE");
        notification.setEmailSent(false);

        repository.save(notification);
    }

    @Test
    void testSaveNotification() {
        Notification saved = repository.save(notification);
        assertNotNull(saved.getId());
    }

    @Test
    void testFindById() {
        Notification found = repository.findById(notification.getId()).orElse(null);
        assertNotNull(found);
    }

    @Test
    void testDeleteNotification() {
        repository.delete(notification);
        assertFalse(repository.findById(notification.getId()).isPresent());
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        List<Notification> list =
                repository.findByUserIdOrderByCreatedAtDesc(1L);

        assertFalse(list.isEmpty());
    }

    @Test
    void testFindByUserIdAndIsReadFalseOrderByCreatedAtDesc() {
        List<Notification> list =
                repository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L);

        assertEquals(1, list.size());
    }

    @Test
    void testCountByUserIdAndIsReadFalse() {
        long count = repository.countByUserIdAndIsReadFalse(1L);
        assertEquals(1, count);
    }

    @Test
    void testFindByType() {
        List<Notification> list =
                repository.findByType(NotificationType.PAYMENT_SUCCESS);

        assertFalse(list.isEmpty());
    }

    @Test
    void testFindByRelatedEntity() {
        List<Notification> list =
                repository.findByRelatedEntityIdAndRelatedEntityType(
                        10L,
                        "COURSE"
                );

        assertFalse(list.isEmpty());
    }

    @Test
    void testMarkAllAsReadForUser() {
        int updated = repository.markAllAsReadForUser(1L);
        assertEquals(1, updated);
    }

    @Test
    void testDeleteByUserId() {
        repository.deleteByUserId(1L);

        List<Notification> list =
                repository.findByUserIdOrderByCreatedAtDesc(1L);

        assertTrue(list.isEmpty());
    }

    @Test
    void testCountByUserId() {
        long count = repository.countByUserId(1L);
        assertEquals(1, count);
    }

    @Test
    void testFindByUserIdOrderByCreatedAtAsc() {
        List<Notification> list =
                repository.findByUserIdOrderByCreatedAtAsc(1L);

        assertFalse(list.isEmpty());
    }
}