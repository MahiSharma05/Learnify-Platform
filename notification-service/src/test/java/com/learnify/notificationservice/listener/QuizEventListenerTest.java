package com.learnify.notificationservice.listener;

import com.learnify.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private QuizEventListener listener;

    @Test
    void testPassedQuiz() {

        Map<String, Object> event = new HashMap<>();

        event.put("studentId", 1L);
        event.put("quizTitle", "Java Quiz");
        event.put("quizId", 1L);
        event.put("score", 90);
        event.put("passingScore", 40);
        event.put("passed", true);

        listener.handleQuizSubmitted(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testFailedQuiz() {

        Map<String, Object> event = new HashMap<>();

        event.put("studentId", 1L);
        event.put("score", 20);
        event.put("passingScore", 40);
        event.put("passed", false);

        listener.handleQuizSubmitted(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }
}