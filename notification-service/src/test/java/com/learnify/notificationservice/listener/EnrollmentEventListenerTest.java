package com.learnify.notificationservice.listener;

import com.learnify.notificationservice.dto.NotificationRequest;
import com.learnify.notificationservice.dto.NotificationResponse;
import com.learnify.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EnrollmentEventListener listener;

    private Map<String, Object> event;

    @BeforeEach
    void setUp() {

        event = new HashMap<>();

        event.put("studentId", 1L);
        event.put("studentEmail", "test@gmail.com");
        event.put("studentName", "Mahi");
        event.put("courseTitle", "Spring Boot");
        event.put("courseId", 10L);
        event.put("enrollmentId", 100L);

        NotificationResponse response = new NotificationResponse();
        response.setId(1L);

        when(notificationService.sendNotification(any()))
                .thenReturn(response);
    }

    @Test
    void testEnrollmentEvent() {

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testNotificationRequestUserId() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertEquals(1L, captor.getValue().getUserId());
    }

    @Test
    void testNotificationRequestEmail() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertEquals(
                "test@gmail.com",
                captor.getValue().getUserEmail()
        );
    }

    @Test
    void testNotificationType() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertEquals(
                "ENROLLMENT_CONFIRMED",
                captor.getValue().getType()
        );
    }

    @Test
    void testNotificationTitle() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertEquals(
                "Enrollment Confirmed!",
                captor.getValue().getTitle()
        );
    }

    @Test
    void testRelatedEntityId() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertEquals(
                10L,
                captor.getValue().getRelatedEntityId()
        );
    }

    @Test
    void testRelatedEntityType() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertEquals(
                "COURSE",
                captor.getValue().getRelatedEntityType()
        );
    }

    @Test
    void testSendEmailTrue() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertTrue(captor.getValue().isSendEmail());
    }

    @Test
    void testSendEmailFalseWhenEmailNull() {

        event.put("studentEmail", null);

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertFalse(captor.getValue().isSendEmail());
    }

    @Test
    void testSendEmailFalseWhenEmailBlank() {

        event.put("studentEmail", "");

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertFalse(captor.getValue().isSendEmail());
    }

    @Test
    void testCourseTitleInMessage() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertTrue(
                captor.getValue()
                        .getMessage()
                        .contains("Spring Boot")
        );
    }

    @Test
    void testNullStudentId() {

        event.put("studentId", null);

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testNullCourseId() {

        event.put("courseId", null);

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testNullCourseTitle() {

        event.put("courseTitle", null);

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testNullEnrollmentId() {

        event.put("enrollmentId", null);

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testStudentNamePresent() {

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testLongConversionInteger() {

        event.put("studentId", 1);

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testLongConversionString() {

        event.put("studentId", "1");

        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testNotificationServiceCalledOnce() {

        listener.handleEnrollmentCreated(event);

        verify(notificationService, only())
                .sendNotification(any());
    }

    @Test
    void testMessageNotNull() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertNotNull(captor.getValue().getMessage());
    }

    @Test
    void testTitleNotNull() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertNotNull(captor.getValue().getTitle());
    }

    @Test
    void testNotificationRequestNotNull() {

        listener.handleEnrollmentCreated(event);

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).sendNotification(captor.capture());

        assertNotNull(captor.getValue());
    }

    @Test
    void testMultipleCalls() {

        listener.handleEnrollmentCreated(event);
        listener.handleEnrollmentCreated(event);

        verify(notificationService, times(2))
                .sendNotification(any());
    }
}