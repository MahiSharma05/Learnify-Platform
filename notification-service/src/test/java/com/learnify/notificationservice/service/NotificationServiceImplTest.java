package com.learnify.notificationservice.service;

import com.learnify.notificationservice.dto.BulkNotificationRequest;
import com.learnify.notificationservice.dto.NotificationRequest;
import com.learnify.notificationservice.dto.NotificationResponse;
import com.learnify.notificationservice.entity.Notification;
import com.learnify.notificationservice.enums.NotificationType;
import com.learnify.notificationservice.exception.ResourceNotFoundException;
import com.learnify.notificationservice.exception.UnauthorizedException;
import com.learnify.notificationservice.repository.NotificationRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl service;

    private Notification notification;
    private NotificationRequest request;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(service, "emailEnabled", false);
        ReflectionTestUtils.setField(service, "maxPerUser", 100);

        notification = new Notification();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setType(NotificationType.PAYMENT_SUCCESS);
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        request = NotificationRequest.builder()
                .userId(1L)
                .type("PAYMENT_SUCCESS")
                .title("Title")
                .message("Message")
                .build();
    }

    @Test
    void testSendNotification() {

        when(repository.save(any(Notification.class)))
                .thenReturn(notification);

        when(repository.countByUserId(1L))
                .thenReturn(1L);

        NotificationResponse response =
                service.sendNotification(request);

        assertNotNull(response);

        verify(repository, times(1))
                .save(any(Notification.class));
    }

    @Test
    void testInvalidTypeThrowsException() {

        request.setType("INVALID");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendNotification(request)
        );
    }

    @Test
    void testGetNotificationsByUser() {

        when(repository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> list =
                service.getNotificationsByUser(1L);

        assertEquals(1, list.size());
    }

    @Test
    void testGetUnreadNotifications() {

        when(repository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        assertEquals(
                1,
                service.getUnreadNotifications(1L).size()
        );
    }

    @Test
    void testGetUnreadCount() {

        when(repository.countByUserIdAndIsReadFalse(1L))
                .thenReturn(5L);

        assertEquals(5, service.getUnreadCount(1L));
    }

    @Test
    void testGetNotificationById() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                service.getNotificationById(
                        1L,
                        1L,
                        "STUDENT"
                );

        assertEquals("Title", response.getTitle());
    }

    @Test
    void testNotificationNotFound() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getNotificationById(
                        1L,
                        1L,
                        "ADMIN"
                )
        );
    }

    @Test
    void testMarkAsRead() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(repository.save(any(Notification.class)))
                .thenReturn(notification);

        NotificationResponse response =
                service.markAsRead(1L, 1L);

        assertNotNull(response);
    }

    @Test
    void testDeleteNotification() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        service.deleteNotification(
                1L,
                1L,
                "STUDENT"
        );

        verify(repository, times(1))
                .delete(notification);
    }

    @Test
    void testDeleteNotificationUnauthorized() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        assertThrows(
                UnauthorizedException.class,
                () -> service.deleteNotification(
                        1L,
                        2L,
                        "STUDENT"
                )
        );
    }

    @Test
    void testBulkNotificationAdmin() {

        BulkNotificationRequest bulk =
                new BulkNotificationRequest();

        bulk.setUserIds(List.of(1L, 2L));
        bulk.setType("PAYMENT_SUCCESS");
        bulk.setTitle("Bulk");
        bulk.setMessage("Bulk Msg");

        when(repository.save(any(Notification.class)))
                .thenReturn(notification);

        when(repository.countByUserId(anyLong()))
                .thenReturn(1L);

        assertNotNull(
                service.sendBulkNotification(
                        bulk,
                        "ADMIN"
                )
        );
    }
}