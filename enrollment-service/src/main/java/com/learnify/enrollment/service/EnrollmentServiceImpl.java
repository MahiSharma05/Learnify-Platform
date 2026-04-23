package com.learnify.enrollment.service;

import com.learnify.enrollment.dto.EnrollmentRequest;
import com.learnify.enrollment.dto.EnrollmentResponse;
import com.learnify.enrollment.dto.ProgressUpdateRequest;
import com.learnify.enrollment.entity.Enrollment;
import com.learnify.enrollment.enums.EnrollmentStatus;
import com.learnify.enrollment.exception.DuplicateEnrollmentException;
import com.learnify.enrollment.exception.ResourceNotFoundException;
import com.learnify.enrollment.exception.UnauthorizedException;
import com.learnify.enrollment.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements com.learnify.enrollment.service.EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    // Constructor injection — consistent with project pattern
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    // ── Enroll ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request,
                                     String studentEmail, Long studentId) {

        // 🔒 Prevent duplicate enrollment
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, request.getCourseId())) {
            throw new DuplicateEnrollmentException(
                    "You are already enrolled in this course (courseId: " + request.getCourseId() + ")"
            );
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStudentEmail(studentEmail);
        enrollment.setCourseTitle(request.getCourseTitle());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercent(0);
        enrollment.setCertificateIssued(false);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return mapToResponse(saved);
    }

    // ── Unenroll ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void unenroll(Long enrollmentId, String userEmail, String userRole) {

        Enrollment enrollment = findOrThrow(enrollmentId);

        // 🔒 Only the enrolled student or ADMIN can unenroll
        if (!enrollment.getStudentEmail().equals(userEmail) && !isAdmin(userRole)) {
            throw new UnauthorizedException("You are not allowed to unenroll this enrollment");
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId, String userRole) {

        // 🔒 Only INSTRUCTOR or ADMIN can view all enrollments for a course
        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException(
                    "Only instructors or admins can view course enrollments");
        }

        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public EnrollmentResponse getEnrollmentById(Long enrollmentId,
                                                String userEmail, String userRole) {
        Enrollment enrollment = findOrThrow(enrollmentId);

        // 🔒 Only the student themselves or ADMIN/INSTRUCTOR can view
        if (!enrollment.getStudentEmail().equals(userEmail) && !isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Access denied for this enrollment");
        }

        return mapToResponse(enrollment);
    }

    // ── Progress & Completion ─────────────────────────────────────────────────

    @Override
    @Transactional
    public EnrollmentResponse updateProgress(Long enrollmentId,
                                             ProgressUpdateRequest request,
                                             String userEmail) {
        Enrollment enrollment = findOrThrow(enrollmentId);

        // 🔒 Only the enrolled student can update their own progress
        if (!enrollment.getStudentEmail().equals(userEmail)) {
            throw new UnauthorizedException("You can only update your own progress");
        }

        enrollment.setProgressPercent(request.getProgressPercent());

        // Auto-complete if progress reaches 100%
        if (request.getProgressPercent() == 100
                && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse markComplete(Long enrollmentId, String userEmail) {

        Enrollment enrollment = findOrThrow(enrollmentId);

        if (!enrollment.getStudentEmail().equals(userEmail)) {
            throw new UnauthorizedException("You can only complete your own enrollment");
        }

        enrollment.setProgressPercent(100);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ── Certificate Issuance ─────────────────────────────────────────────────

    @Override
    @Transactional
    public EnrollmentResponse issueCertificate(Long enrollmentId,
                                               String certificateUrl,
                                               String userEmail) {
        Enrollment enrollment = findOrThrow(enrollmentId);

        // 🔒 Must be completed before a certificate is issued
        if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new UnauthorizedException(
                    "Certificate can only be issued after course completion");
        }

        if (enrollment.isCertificateIssued()) {
            throw new DuplicateEnrollmentException(
                    "Certificate has already been issued for this enrollment");
        }

        enrollment.setCertificateIssued(true);
        enrollment.setCertificateUrl(certificateUrl);

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ── Analytics ────────────────────────────────────────────────────────────

    @Override
    public long getEnrollmentCount(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Enrollment findOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found with id: " + id));
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isInstructorOrAdmin(String role) {
        return "INSTRUCTOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    // Map Enrollment entity → EnrollmentResponse DTO
    private EnrollmentResponse mapToResponse(Enrollment e) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(e.getId());
        response.setStudentId(e.getStudentId());
        response.setCourseId(e.getCourseId());
        response.setStudentEmail(e.getStudentEmail());
        response.setCourseTitle(e.getCourseTitle());
        response.setEnrolledAt(e.getEnrolledAt());
        response.setCompletedAt(e.getCompletedAt());
        response.setStatus(e.getStatus().name());
        response.setProgressPercent(e.getProgressPercent());
        response.setCertificateIssued(e.isCertificateIssued());
        response.setCertificateUrl(e.getCertificateUrl());
        return response;
    }
}