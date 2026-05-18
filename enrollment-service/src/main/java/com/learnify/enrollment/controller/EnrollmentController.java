package com.learnify.enrollment.controller;

import com.learnify.enrollment.dto.EnrollmentRequest;
import com.learnify.enrollment.dto.EnrollmentResponse;
import com.learnify.enrollment.dto.ProgressUpdateRequest;
import com.learnify.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.learnify.enrollment.repository.EnrollmentRepository;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentController(EnrollmentService enrollmentService,
                                EnrollmentRepository enrollmentRepository) {
        this.enrollmentService = enrollmentService;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * POST /api/enrollments
     * Enroll the logged-in student in a course.
     * Roles: STUDENT
     *
     * Headers from Gateway:
     *   X-User-Email  → student's email
     *   X-User-Id     → student's userId (Long)
     *   X-User-Role   → must be STUDENT
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(
            @Valid @RequestBody EnrollmentRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Role")  String role) {

        // Role check — only students enroll themselves
        if (!"STUDENT".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.status(201)
                .body(enrollmentService.enroll(request, email, studentId));
    }

    /**
     * DELETE /api/enrollments/{id}
     * Unenroll from a course (sets status to CANCELLED).
     * Roles: STUDENT (own), ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> unenroll(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        enrollmentService.unenroll(id, email, role);
        return ResponseEntity.ok("Unenrolled successfully");
    }

    /**
     * GET /api/enrollments/my
     * Get all enrollments for the logged-in student.
     * Roles: STUDENT
     */
//    @GetMapping("/my")
//    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(Authentication authentication) {
//
//        String email = authentication.getName(); // from JWT
//
//        return ResponseEntity.ok(enrollmentService.getEnrollmentsByEmail(email));
//    }
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String email) {

        // Use studentId directly from header (set by Gateway) for efficiency.
        // Falls back to email-based lookup via UserClient if needed.
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    /**
     * GET /api/enrollments/course/{courseId}
     * Get all enrollments for a course.
     * Roles: INSTRUCTOR, ADMIN
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentResponse>> getByCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId, role));
    }

    /**
     * GET /api/enrollments/{id}
     * Get a single enrollment by ID.
     * Roles: STUDENT (own), INSTRUCTOR, ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id, email, role));
    }

    /**
     * GET /api/enrollments/check/{courseId}
     * Check if the logged-in student is enrolled in a course.
     * Roles: STUDENT
     * Returns: { "enrolled": true/false }
     */
    @GetMapping("/check/{courseId}")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long studentId) {

        boolean enrolled = enrollmentService.isEnrolled(studentId, courseId);
        return ResponseEntity.ok(Map.of("enrolled", enrolled));
    }

    /**
     * PUT /api/enrollments/{id}/progress
     * Update progress percentage for an enrollment.
     * Roles: STUDENT (own)
     * Called by progress-service or directly by student watch events.
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<EnrollmentResponse> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody ProgressUpdateRequest request,
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(enrollmentService.updateProgress(id, request, email));
    }

    /**
     * PUT /api/enrollments/{id}/complete
     * Mark enrollment as completed (progress = 100%).
     * Roles: STUDENT (own)
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<EnrollmentResponse> markComplete(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(enrollmentService.markComplete(id, email));
    }

    /**
     * POST /api/enrollments/{id}/certificate
     * Issue a certificate for a completed enrollment.
     * Roles: STUDENT (own), ADMIN
     * Body: { "certificateUrl": "https://..." }
     */
    @PostMapping("/{id}/certificate")
    public ResponseEntity<EnrollmentResponse> issueCertificate(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("X-User-Email") String email) {

        String certificateUrl = body.get("certificateUrl");
        return ResponseEntity.ok(
                enrollmentService.issueCertificate(id, certificateUrl, email));
    }

    /**
     * GET /api/enrollments/course/{courseId}/count
     * Get total enrollment count for a course (analytics).
     * Roles: INSTRUCTOR, ADMIN
     * Returns: { "count": 42 }
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Map<String, Long>> getEnrollmentCount(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(Map.of("count", enrollmentService.getEnrollmentCount(courseId)));
    }
    /**
     * GET /api/enrollments/all
     * Get all enrollments platform-wide.
     * Roles: ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(enrollmentRepository.findAll()
                .stream()
                .map(e -> enrollmentService.getEnrollmentById(e.getId(), e.getStudentEmail(), "ADMIN"))
                .toList());
    }
}