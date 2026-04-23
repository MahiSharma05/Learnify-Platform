package com.learnify.enrollment.service;

import com.learnify.enrollment.dto.EnrollmentRequest;
import com.learnify.enrollment.dto.EnrollmentResponse;
import com.learnify.enrollment.dto.ProgressUpdateRequest;

import java.util.List;

public interface EnrollmentService {

    // Enroll a student in a course (STUDENT only)
    EnrollmentResponse enroll(EnrollmentRequest request, String studentEmail, Long studentId);

    // Unenroll / cancel an enrollment
    void unenroll(Long enrollmentId, String userEmail, String userRole);

    // Get all enrollments for the logged-in student
    List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId);

    // Get all enrollments for a course (INSTRUCTOR / ADMIN)
    List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId, String userRole);

    // Check if a student is enrolled in a specific course
    boolean isEnrolled(Long studentId, Long courseId);

    // Update course progress percent (called by progress-service)
    EnrollmentResponse updateProgress(Long enrollmentId, ProgressUpdateRequest request,
                                      String userEmail);

    // Mark enrollment as COMPLETED (called when progress reaches 100%)
    EnrollmentResponse markComplete(Long enrollmentId, String userEmail);

    // Issue certificate for a completed enrollment
    EnrollmentResponse issueCertificate(Long enrollmentId, String certificateUrl,
                                        String userEmail);

    // Get total enrollment count for a course (analytics)
    long getEnrollmentCount(Long courseId);

    // Get a single enrollment by ID
    EnrollmentResponse getEnrollmentById(Long enrollmentId, String userEmail, String userRole);
}