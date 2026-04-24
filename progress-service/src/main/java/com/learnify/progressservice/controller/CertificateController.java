package com.learnify.progressservice.controller;

import com.learnify.progressservice.dto.CertificateResponse;
import com.learnify.progressservice.exception.CertificateException;
import com.learnify.progressservice.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final ProgressService progressService;

    public CertificateController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * POST /api/certificates/issue
     * Issue a certificate for a completed course.
     * MUST check 100% completion BEFORE calling this endpoint.
     * Roles: STUDENT (own), ADMIN
     *
     * Body:
     * {
     *   "courseId": 1,
     *   "studentName": "John Doe",
     *   "courseTitle": "Java Masterclass",
     *   "instructorName": "Jane Smith"
     * }
     */
    @PostMapping("/issue")
    public ResponseEntity<CertificateResponse> issueCertificate(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        if (!"STUDENT".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        Long courseId        = Long.parseLong(body.get("courseId").toString());
        String studentName   = (String) body.getOrDefault("studentName", email);
        String courseTitle   = (String) body.getOrDefault("courseTitle", "Course");
        String instructorName = (String) body.getOrDefault("instructorName", issuerName());

        // 🔒 Verify 100% completion before issuing
        var courseProgress = progressService.getCourseProgress(
                studentId, courseId, 0);

        if (!courseProgress.isCourseCompleted() && courseProgress.getCompletionPercent() < 100) {
            throw new CertificateException(
                    "Course not completed. Current progress: "
                            + courseProgress.getCompletionPercent() + "%");
        }

        return ResponseEntity.status(201).body(
                progressService.issueCertificate(studentId, email, courseId,
                        studentName, courseTitle, instructorName));
    }

    /**
     * GET /api/certificates/my
     * Get all certificates for the logged-in student.
     * Roles: STUDENT
     */
    @GetMapping("/my")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(progressService.getCertificatesByStudent(studentId));
    }

    /**
     * GET /api/certificates/{id}
     * Get a certificate by ID.
     * Roles: STUDENT (own), ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> getCertificateById(
            @PathVariable Long id) {

        return ResponseEntity.ok(progressService.getCertificateById(id));
    }

    /**
     * GET /api/certificates/course/{courseId}
     * Get the certificate for a student's specific course.
     * Roles: STUDENT
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CertificateResponse> getCertificateByCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(progressService.getCertificate(studentId, courseId));
    }

    /**
     * GET /api/certificates/verify/{code}
     * ✅ PUBLIC ENDPOINT — No authentication required.
     * Verify a certificate by its unique UUID verification code.
     * Used by employers/institutions to validate credentials.
     *
     * Returns certificate details if valid, 404 if invalid/revoked.
     */
    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateResponse> verifyCertificate(
            @PathVariable String code) {

        return ResponseEntity.ok(progressService.verifyCertificate(code));
    }

    /**
     * GET /api/certificates/all
     * Get all issued certificates across the platform.
     * Roles: ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<List<CertificateResponse>> getAllCertificates(
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(progressService.getAllCertificates(role));
    }

    // Helper to get issuer name for certificate generation
    private String issuerName() {
        return "Learnify Platform";
    }
}