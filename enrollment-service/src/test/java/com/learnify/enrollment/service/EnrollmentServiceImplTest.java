package com.learnify.enrollment.service;

import com.learnify.enrollment.client.UserClient;
import com.learnify.enrollment.dto.EnrollmentRequest;
import com.learnify.enrollment.dto.EnrollmentResponse;
import com.learnify.enrollment.dto.ProgressUpdateRequest;
import com.learnify.enrollment.entity.Enrollment;
import com.learnify.enrollment.enums.EnrollmentStatus;
import com.learnify.enrollment.event.EnrollmentEventPublisher;
import com.learnify.enrollment.exception.DuplicateEnrollmentException;
import com.learnify.enrollment.exception.UnauthorizedException;
import com.learnify.enrollment.repository.EnrollmentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private EnrollmentEventPublisher enrollmentEventPublisher;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    // ============================
    // TEST 1 : ENROLL SUCCESS
    // ============================
    @Test
    void shouldEnrollStudentSuccessfully() {

        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourseId(1L);
        request.setCourseTitle("Java");

        when(enrollmentRepository
                .existsByStudentIdAndCourseId(1L, 1L))
                .thenReturn(false);

        Enrollment saved = new Enrollment();
        saved.setId(1L);
        saved.setStudentId(1L);
        saved.setCourseId(1L);
        saved.setStudentEmail("mahi@gmail.com");
        saved.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(saved);

        EnrollmentResponse response =
                enrollmentService.enroll(
                        request,
                        "mahi@gmail.com",
                        1L
                );

        assertNotNull(response);
        assertEquals(1L, response.getCourseId());
    }

    // =================================
    // TEST 2 : DUPLICATE ENROLLMENT
    // =================================
    @Test
    void shouldThrowDuplicateEnrollmentException() {

        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourseId(1L);

        when(enrollmentRepository
                .existsByStudentIdAndCourseId(1L, 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateEnrollmentException.class,
                () -> enrollmentService.enroll(
                        request,
                        "mahi@gmail.com",
                        1L
                )
        );
    }

    // ============================
    // TEST 3 : UPDATE PROGRESS
    // ============================
    @Test
    void shouldUpdateProgressSuccessfully() {

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setStudentEmail("mahi@gmail.com");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findById(1L))
                .thenReturn(Optional.of(enrollment));

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(enrollment);

        ProgressUpdateRequest request =
                new ProgressUpdateRequest();

        request.setProgressPercent(80);

        EnrollmentResponse response =
                enrollmentService.updateProgress(
                        1L,
                        request,
                        "mahi@gmail.com"
                );

        assertEquals(80, response.getProgressPercent());
    }

    // ============================
    // TEST 4 : UNAUTHORIZED ACCESS
    // ============================
    @Test
    void shouldThrowUnauthorizedException() {

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setStudentEmail("other@gmail.com");

        when(enrollmentRepository.findById(1L))
                .thenReturn(Optional.of(enrollment));

        ProgressUpdateRequest request =
                new ProgressUpdateRequest();

        request.setProgressPercent(50);

        assertThrows(
                UnauthorizedException.class,
                () -> enrollmentService.updateProgress(
                        1L,
                        request,
                        "mahi@gmail.com"
                )
        );
    }
}