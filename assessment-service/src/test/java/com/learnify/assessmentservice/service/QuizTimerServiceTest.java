package com.learnify.assessmentservice.service;

import com.learnify.assessmentservice.dto.QuizTimerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuizTimerServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private QuizTimerService quizTimerService;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        quizTimerService = new QuizTimerService(redisTemplate);

        ReflectionTestUtils.setField(
                quizTimerService,
                "graceSeconds",
                60
        );

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    void shouldStartTimer() {

        when(redisTemplate.hasKey(anyString()))
                .thenReturn(false);

        QuizTimerState state =
                quizTimerService.startTimer(
                        1L,
                        1L,
                        10L,
                        300
                );

        assertNotNull(state);
        assertEquals(1L, state.getAttemptId());

        verify(valueOperations, times(1))
                .set(anyString(), any(), any());
    }

    @Test
    void shouldReturnEmptyWhenTimerNotFound() {

        when(redisTemplate.opsForValue().get(anyString()))
                .thenReturn(null);

        Optional<QuizTimerState> result =
                quizTimerService.getTimer(1L);

        assertTrue(result.isEmpty());
    }
}