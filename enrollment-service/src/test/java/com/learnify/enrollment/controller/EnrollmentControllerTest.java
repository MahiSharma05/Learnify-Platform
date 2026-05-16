package com.learnify.enrollment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.enrollment.dto.EnrollmentRequest;
import com.learnify.enrollment.dto.EnrollmentResponse;
import com.learnify.enrollment.repository.EnrollmentRepository;
import com.learnify.enrollment.service.EnrollmentService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    // =================================
    // TEST 1 : ENROLL SUCCESS
    // =================================
    @Test
    void shouldEnrollSuccessfully() throws Exception {

        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourseId(1L);
        request.setCourseTitle("Java");

        EnrollmentResponse response =
                new EnrollmentResponse();

        response.setCourseId(1L);

        when(enrollmentService.enroll(
                any(),
                any(),
                any()
        )).thenReturn(response);

        mockMvc.perform(post("/api/enrollments")
                        .header("X-User-Email", "mahi@gmail.com")
                        .header("X-User-Id", 1L)
                        .header("X-User-Role", "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // =================================
    // TEST 2 : FORBIDDEN ENROLL
    // =================================
    @Test
    void shouldReturnForbiddenForNonStudent() throws Exception {

        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourseId(1L);

        mockMvc.perform(post("/api/enrollments")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Id", 1L)
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // =================================
    // TEST 3 : GET MY ENROLLMENTS
    // =================================
    @Test
    void shouldGetMyEnrollments() throws Exception {

        EnrollmentResponse response =
                new EnrollmentResponse();

        response.setCourseId(1L);

        when(enrollmentService
                .getEnrollmentsByStudentId(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/enrollments/my")
                        .header("X-User-Id", 1L)
                        .header("X-User-Email", "mahi@gmail.com"))
                .andExpect(status().isOk());
    }

    // =================================
    // TEST 4 : CHECK ENROLLMENT
    // =================================
    @Test
    void shouldCheckEnrollment() throws Exception {

        when(enrollmentService
                .isEnrolled(1L, 1L))
                .thenReturn(true);

        mockMvc.perform(get("/api/enrollments/check/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }
}