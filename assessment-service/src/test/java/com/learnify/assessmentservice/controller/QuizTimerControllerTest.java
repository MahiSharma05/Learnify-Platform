package com.learnify.assessmentservice.controller;

import com.learnify.assessmentservice.dto.QuizTimerState;
import com.learnify.assessmentservice.service.QuizTimerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = QuizTimerController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class QuizTimerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizTimerService quizTimerService;

    @Test
    @DisplayName("Should get remaining time")
    void shouldGetRemainingTime() throws Exception {

        when(quizTimerService.getRemainingSeconds(1L))
                .thenReturn(250L);

        when(quizTimerService.isExpired(1L))
                .thenReturn(false);

        mockMvc.perform(get("/api/quiz-timer/1/remaining"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingSeconds").value(250))
                .andExpect(jsonPath("$.isExpired").value(false));
    }

    @Test
    @DisplayName("Should save answers")
    void shouldSaveAnswers() throws Exception {

        when(quizTimerService.isExpired(1L))
                .thenReturn(false);

        when(quizTimerService.getRemainingSeconds(1L))
                .thenReturn(200L);

        mockMvc.perform(post("/api/quiz-timer/1/answers")
                        .contentType("application/json")
                        .content("""
                                {
                                  "1":"A",
                                  "2":"True"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(true));
    }

    @Test
    @DisplayName("Should return expired state")
    void shouldReturnExpiredState() throws Exception {

        when(quizTimerService.isExpired(1L))
                .thenReturn(true);

        mockMvc.perform(post("/api/quiz-timer/1/answers")
                        .contentType("application/json")
                        .content("""
                                {
                                  "1":"A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(false))
                .andExpect(jsonPath("$.isExpired").value(true));
    }

    @Test
    @DisplayName("Should get timer state")
    void shouldGetTimerState() throws Exception {

        QuizTimerState state = QuizTimerState.builder()
                .attemptId(1L)
                .quizId(1L)
                .studentId(10L)
                .build();

        when(quizTimerService.getTimer(1L))
                .thenReturn(Optional.of(state));

        mockMvc.perform(get("/api/quiz-timer/1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(1));
    }

    @Test
    @DisplayName("Should return 404 when timer not found")
    void shouldReturn404WhenTimerNotFound() throws Exception {

        when(quizTimerService.getTimer(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/quiz-timer/1/state"))
                .andExpect(status().isNotFound());
    }
}