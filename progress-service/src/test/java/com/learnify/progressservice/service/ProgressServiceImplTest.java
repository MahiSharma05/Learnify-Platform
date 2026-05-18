package com.learnify.progressservice.service;

import com.learnify.progressservice.dto.ProgressResponse;
import com.learnify.progressservice.dto.ProgressTrackRequest;
import com.learnify.progressservice.entity.Progress;
import com.learnify.progressservice.repository.CertificateRepository;
import com.learnify.progressservice.repository.ProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

    @Test
    void trackProgress_ShouldCreateNewProgress() {

        ProgressTrackRequest request = new ProgressTrackRequest();
        request.setCourseId(1L);
        request.setLessonId(5L);
        request.setWatchedSeconds(120);

        when(progressRepository.findByStudentIdAndLessonId(1L, 5L))
                .thenReturn(Optional.empty());

        Progress saved = new Progress();
        saved.setId(1L);
        saved.setCourseId(1L);
        saved.setLessonId(5L);
        saved.setWatchedSeconds(120);

        when(progressRepository.save(any(Progress.class)))
                .thenReturn(saved);

        ProgressResponse response = progressService.trackProgress(
                1L,
                "test@gmail.com",
                request
        );

        assertNotNull(response);
        assertEquals(120, response.getWatchedSeconds());
        assertEquals(5L, response.getLessonId());
    }

    @Test
    void getLessonProgress_ShouldThrowException_WhenNotFound() {

        when(progressRepository.findByStudentIdAndLessonId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            progressService.getLessonProgress(1L, 99L);
        });
    }
}