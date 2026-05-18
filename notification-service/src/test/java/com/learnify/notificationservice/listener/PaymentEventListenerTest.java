// src/test/java/com/learnify/notificationservice/listener/PaymentEventListenerTest.java

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
class PaymentEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentEventListener listener;

    @Test
    void testCoursePurchasePayment() {

        Map<String, Object> event = new HashMap<>();

        event.put("studentId", 1L);
        event.put("paymentType", "COURSE_PURCHASE");
        event.put("courseTitle", "Java");
        event.put("amount", 999);

        listener.handlePaymentSuccess(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }

    @Test
    void testSubscriptionPayment() {

        Map<String, Object> event = new HashMap<>();

        event.put("studentId", 1L);
        event.put("paymentType", "SUBSCRIPTION");
        event.put("subscriptionPlan", "PREMIUM");
        event.put("amount", 1999);

        listener.handlePaymentSuccess(event);

        verify(notificationService, times(1))
                .sendNotification(any());
    }
}