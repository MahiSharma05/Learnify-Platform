package com.learnify.progressservice.service;

import com.learnify.progressservice.dto.*;

import java.util.List;

public interface ProgressService {

    // ── Progress Tracking ─────────────────────────────────────────────────────

    /**
     * Track watch time for a lesson.
     * Creates a progress record on first access, then accumulates watchedSeconds.
     * Auto-marks complete if watchedSeconds >= 90% of lesson duration.
     */
    ProgressResponse trackProgress(Long studentId, String studentEmail,
                                   ProgressTrackRequest request);

    /**
     * Explicitly mark a lesson as completed.
     * Sets completed = true and completedAt = now.
     */
    ProgressResponse markLessonComplete(Long studentId, String studentEmail,
                                        Long courseId, Long lessonId);

    /**
     * Get the aggregated course progress for a student.
     * Includes % complete and per-lesson breakdown.
     *
     * @param totalLessonsInCourse Pass total lesson count from lesson-service.
     *                              If 0, calculates from tracked lessons only.
     */
    CourseProgressResponse getCourseProgress(Long studentId, Long courseId,
                                             long totalLessonsInCourse);

    /**
     * Get progress record for a specific lesson.
     */
    ProgressResponse getLessonProgress(Long studentId, Long lessonId);

    /**
     * Get all progress records for a student (across all courses).
     */
    List<ProgressResponse> getAllProgressByStudent(Long studentId);

    // ── Certificate Operations ────────────────────────────────────────────────

    /**
     * Issue a certificate for a completed course.
     * Throws CertificateException if:
     *   - Course not 100% complete
     *   - Certificate already issued
     */
    CertificateResponse issueCertificate(Long studentId, String studentEmail,
                                         Long courseId, String studentName,
                                         String courseTitle, String instructorName);

    /**
     * Get certificate for a student + course.
     */
    CertificateResponse getCertificate(Long studentId, Long courseId);

    /**
     * Get certificate by ID.
     */
    CertificateResponse getCertificateById(Long certificateId);

    /**
     * Get all certificates earned by a student.
     */
    List<CertificateResponse> getCertificatesByStudent(Long studentId);

    /**
     * PUBLIC — Verify certificate by unique verification code.
     * No authentication required.
     */
    CertificateResponse verifyCertificate(String verificationCode);

    /**
     * Get all issued certificates (ADMIN).
     */
    List<CertificateResponse> getAllCertificates(String userRole);
}