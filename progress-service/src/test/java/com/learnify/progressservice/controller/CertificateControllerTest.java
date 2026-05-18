package com.learnify.progressservice.controller;

import com.learnify.progressservice.dto.CertificateResponse;
import com.learnify.progressservice.dto.CourseProgressResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CertificateController.class)
@AutoConfigureMockMvc(addFilters = false)
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgressService progressService;

    // =========================================================
    // TEST 1
    // Issue Certificate Success
    // =========================================================

    @Test
    @DisplayName("Issue Certificate - Success")
    void issueCertificate_ShouldReturn201() throws Exception {

        CourseProgressResponse progress = new CourseProgressResponse();
        progress.setCourseCompleted(true);
        progress.setCompletionPercent(100);

        CertificateResponse response = new CertificateResponse();
        response.setCourseId(1L);

        when(progressService.getCourseProgress(
                anyLong(),
                anyLong(),
                anyLong()))
                .thenReturn(progress);

        when(progressService.issueCertificate(
                anyLong(),
                anyString(),
                anyLong(),
                anyString(),
                anyString(),
                anyString()))
                .thenReturn(response);

        String body = """
                {
                    "courseId":1,
                    "studentName":"Mahi",
                    "courseTitle":"Java",
                    "instructorName":"John"
                }
                """;

        mockMvc.perform(post("/api/certificates/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isCreated());
    }

    // =========================================================
    // TEST 2
    // Invalid Role
    // =========================================================

    @Test
    @DisplayName("Issue Certificate - Invalid Role")
    void issueCertificate_ShouldReturn403_WhenRoleInvalid() throws Exception {

        String body = """
                {
                    "courseId":1
                }
                """;

        mockMvc.perform(post("/api/certificates/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("X-User-Id", 1)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "INSTRUCTOR"))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // TEST 3
    // Get My Certificates
    // =========================================================

    @Test
    @DisplayName("Get My Certificates")
    void getMyCertificates_ShouldReturn200() throws Exception {

        CertificateResponse response = new CertificateResponse();
        response.setCourseId(1L);

        when(progressService.getCertificatesByStudentId(anyLong()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/certificates/my")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // =========================================================
    // TEST 4
    // Get Certificate By ID
    // =========================================================

    @Test
    @DisplayName("Get Certificate By ID")
    void getCertificateById_ShouldReturn200() throws Exception {

        CertificateResponse response = new CertificateResponse();
        response.setId(1L);

        when(progressService.getCertificateById(anyLong()))
                .thenReturn(response);

        mockMvc.perform(get("/api/certificates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // =========================================================
    // TEST 5
    // Verify Certificate
    // =========================================================

    @Test
    @DisplayName("Verify Certificate")
    void verifyCertificate_ShouldReturn200() throws Exception {

        CertificateResponse response = new CertificateResponse();
        response.setVerificationCode("abc123");

        when(progressService.verifyCertificate(anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/certificates/verify/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationCode").value("abc123"));
    }

    // =========================================================
    // TEST 6
    // Get Certificate By Course
    // =========================================================

    @Test
    @DisplayName("Get Certificate By Course")
    void getCertificateByCourse_ShouldReturn200() throws Exception {

        CertificateResponse response = new CertificateResponse();
        response.setCourseId(1L);

        when(progressService.getCertificate(anyLong(), anyLong()))
                .thenReturn(response);

        mockMvc.perform(get("/api/certificates/course/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1));
    }

    // =========================================================
    // TEST 7
    // Get All Certificates
    // =========================================================

    @Test
    @DisplayName("Get All Certificates")
    void getAllCertificates_ShouldReturn200() throws Exception {

        CertificateResponse response = new CertificateResponse();
        response.setCourseId(1L);

        when(progressService.getAllCertificates(anyString()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/certificates/all")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}