package com.learnify.progressservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.progressservice.dto.CourseProgressResponse;
import com.learnify.progressservice.dto.ProgressResponse;
import com.learnify.progressservice.dto.ProgressTrackRequest;
import com.learnify.progressservice.service.ProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // TEST 1
    // Track Progress Success
    // =========================================================

    @Test
    @DisplayName("Track Progress - Success")
    void trackProgress_ShouldReturn200() throws Exception {

        ProgressTrackRequest request = new ProgressTrackRequest();
        request.setCourseId(1L);
        request.setLessonId(10L);
        request.setWatchedSeconds(120);

        ProgressResponse response = new ProgressResponse();
        response.setCourseId(1L);
        response.setLessonId(10L);
        response.setWatchedSeconds(120);

        when(progressService.trackProgress(
                anyLong(),
                anyString(),
                any(ProgressTrackRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/progress/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.lessonId").value(10))
                .andExpect(jsonPath("$.watchedSeconds").value(120));
    }

    // =========================================================
    // TEST 2
    // Invalid Role
    // =========================================================

    @Test
    @DisplayName("Track Progress - Invalid Role")
    void trackProgress_ShouldReturn403_WhenRoleInvalid() throws Exception {

        ProgressTrackRequest request = new ProgressTrackRequest();
        request.setCourseId(1L);
        request.setLessonId(10L);
        request.setWatchedSeconds(120);

        mockMvc.perform(post("/api/progress/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // TEST 3
    // Validation Failure
    // =========================================================

    @Test
    @DisplayName("Track Progress - Validation Failure")
    void trackProgress_ShouldReturn400_WhenRequestInvalid() throws Exception {

        ProgressTrackRequest request = new ProgressTrackRequest();

        request.setCourseId(null);
        request.setLessonId(null);
        request.setWatchedSeconds(null);

        mockMvc.perform(post("/api/progress/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // TEST 4
    // Mark Lesson Complete
    // =========================================================

    @Test
    @DisplayName("Mark Lesson Complete - Success")
    void markLessonComplete_ShouldReturn200() throws Exception {

        ProgressResponse response = new ProgressResponse();
        response.setCourseId(1L);
        response.setLessonId(5L);
        response.setCompleted(true);

        when(progressService.markLessonComplete(
                anyLong(),
                anyString(),
                anyLong(),
                anyLong()))
                .thenReturn(response);

        Map<String, Long> requestBody = Map.of(
                "courseId", 1L,
                "lessonId", 5L
        );

        mockMvc.perform(put("/api/progress/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    // =========================================================
    // TEST 5
    // Get Course Progress
    // =========================================================

    @Test
    @DisplayName("Get Course Progress - Success")
    void getCourseProgress_ShouldReturn200() throws Exception {

        CourseProgressResponse response = new CourseProgressResponse();
        response.setCourseId(1L);
        response.setCompletionPercent(80);
        response.setCompletedLessons(4);
        response.setTotalLessons(5);

        when(progressService.getCourseProgress(anyLong(), anyLong(), anyLong()))
                .thenReturn(response);

        mockMvc.perform(get("/api/progress/course/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.completionPercent").value(80));
    }

    // =========================================================
    // TEST 6
    // Get Lesson Progress
    // =========================================================

    @Test
    @DisplayName("Get Lesson Progress - Success")
    void getLessonProgress_ShouldReturn200() throws Exception {

        ProgressResponse response = new ProgressResponse();
        response.setLessonId(10L);
        response.setWatchedSeconds(300);

        when(progressService.getLessonProgress(anyLong(), anyLong()))
                .thenReturn(response);

        mockMvc.perform(get("/api/progress/lesson/10")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(10))
                .andExpect(jsonPath("$.watchedSeconds").value(300));
    }

    // =========================================================
    // TEST 7
    // Get All Progress
    // =========================================================

    @Test
    @DisplayName("Get All Progress - Success")
    void getAllMyProgress_ShouldReturn200() throws Exception {

        ProgressResponse response1 = new ProgressResponse();
        response1.setLessonId(1L);

        ProgressResponse response2 = new ProgressResponse();
        response2.setLessonId(2L);

        when(progressService.getAllProgressByStudent(anyLong()))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/progress/my")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // =========================================================
    // TEST 8
    // Internal Server Error
    // =========================================================

    @Test
    @DisplayName("Track Progress - Internal Server Error")
    void trackProgress_ShouldReturn400_WhenExceptionOccurs() throws Exception {

        ProgressTrackRequest request = new ProgressTrackRequest();
        request.setCourseId(1L);
        request.setLessonId(10L);
        request.setWatchedSeconds(100);

        when(progressService.trackProgress(
                anyLong(),
                anyString(),
                any(ProgressTrackRequest.class)))
                .thenThrow(new RuntimeException("Database Error"));

        mockMvc.perform(post("/api/progress/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }
}