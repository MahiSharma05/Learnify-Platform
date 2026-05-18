package com.learnify.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.notificationservice.dto.NotificationRequest;
import com.learnify.notificationservice.dto.NotificationResponse;
import com.learnify.notificationservice.entity.Notification;
import com.learnify.notificationservice.enums.NotificationType;
import com.learnify.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com.learnify.notificationservice.config.*"
                )
        }
)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setup() {
        notification = new Notification();

        notification.setId(1L);
        notification.setUserId(1L);
        notification.setTitle("Test Title");
        notification.setMessage("Test Message");
        notification.setType(NotificationType.PAYMENT_SUCCESS);
        notification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void testSendNotification() throws Exception {

        NotificationRequest request = new NotificationRequest();

        request.setUserId(1L);
        request.setTitle("Title");
        request.setMessage("Message");
        request.setType("PAYMENT_SUCCESS");

        NotificationResponse response = new NotificationResponse();
        response.setId(1L);

        Mockito.when(notificationService.sendNotification(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void testGetMyNotifications() throws Exception {

        NotificationResponse response = new NotificationResponse();

        response.setId(1L);
        response.setTitle("Test Title");
        response.setMessage("Test Message");

        Mockito.when(notificationService.getNotificationsByUser(anyLong()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/notifications/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUnreadCount() throws Exception {

        Mockito.when(notificationService.getUnreadCount(anyLong()))
                .thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser
    void testGetMyNotifications_EmptyList() throws Exception {

        Mockito.when(notificationService.getNotificationsByUser(anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUnreadCount_Zero() throws Exception {

        Mockito.when(notificationService.getUnreadCount(anyLong()))
                .thenReturn(0L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUnreadCount_MultipleTimes() throws Exception {

        Mockito.when(notificationService.getUnreadCount(anyLong()))
                .thenReturn(10L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testSendNotification_InvalidType() throws Exception {

        NotificationRequest request = new NotificationRequest();

        request.setUserId(1L);
        request.setTitle("Invalid");
        request.setMessage("Invalid Type");
        request.setType("INVALID_TYPE");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetNotifications_Unauthorized() throws Exception {

        mockMvc.perform(get("/api/notifications/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSendNotification_Unauthorized() throws Exception {

        NotificationRequest request = new NotificationRequest();

        request.setUserId(1L);
        request.setTitle("Title");
        request.setMessage("Message");
        request.setType("PAYMENT_SUCCESS");

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testSendNotification_WithoutCsrf() throws Exception {

        NotificationRequest request = new NotificationRequest();

        request.setUserId(1L);
        request.setTitle("Title");
        request.setMessage("Message");
        request.setType("PAYMENT_SUCCESS");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testGetNotifications_InvalidHeader() throws Exception {

        mockMvc.perform(get("/api/notifications/my")
                        .header("X-User-Id", "abc"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testUnreadCount_InvalidHeader() throws Exception {

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "xyz"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testSendNotification_NullBody() throws Exception {

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetNotifications_MissingHeader() throws Exception {

        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUnreadCount_MissingHeader() throws Exception {

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testSendNotification_ServiceThrowsException() throws Exception {

        NotificationRequest request = new NotificationRequest();

        request.setUserId(1L);
        request.setTitle("Title");
        request.setMessage("Message");
        request.setType("PAYMENT_SUCCESS");

        Mockito.when(notificationService.sendNotification(any()))
                .thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testGetNotifications_ServiceThrowsException() throws Exception {

        Mockito.when(notificationService.getNotificationsByUser(anyLong()))
                .thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/notifications/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void testUnreadCount_ServiceThrowsException() throws Exception {

        Mockito.when(notificationService.getUnreadCount(anyLong()))
                .thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "1"))
                .andExpect(status().isInternalServerError());
    }

}