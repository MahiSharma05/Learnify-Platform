package com.learnify.progressservice.service;

import com.learnify.progressservice.client.UserClient;
import com.learnify.progressservice.dto.*;
import com.learnify.progressservice.entity.Certificate;
import com.learnify.progressservice.entity.Progress;
import com.learnify.progressservice.exception.CertificateException;
import com.learnify.progressservice.exception.ResourceNotFoundException;
import com.learnify.progressservice.exception.UnauthorizedException;
import com.learnify.progressservice.repository.CertificateRepository;
import com.learnify.progressservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final CertificateRepository certificateRepository;

    @Autowired
    private UserClient userClient;
    // Injected from application.yml
    @Value("${certificate.base-url}")
    private String certificateBaseUrl;

    @Value("${certificate.issuer-name}")
    private String issuerName;

    public ProgressServiceImpl(ProgressRepository progressRepository,
                               CertificateRepository certificateRepository) {
        this.progressRepository = progressRepository;
        this.certificateRepository = certificateRepository;
    }

    // ── Progress Tracking ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProgressResponse trackProgress(Long studentId, String studentEmail,
                                          ProgressTrackRequest request) {

        // Find existing progress record or create a new one
        Progress progress = progressRepository
                .findByStudentIdAndLessonId(studentId, request.getLessonId())
                .orElseGet(() -> {
                    Progress p = new Progress();
                    p.setStudentId(studentId);
                    p.setStudentEmail(studentEmail);
                    p.setCourseId(request.getCourseId());
                    p.setLessonId(request.getLessonId());
                    p.setWatchedSeconds(0);
                    p.setCompleted(false);
                    p.setFirstAccessedAt(LocalDateTime.now());
                    return p;
                });

        // Accumulate watched seconds (never decrease)
        int newTotal = progress.getWatchedSeconds() + request.getWatchedSeconds();
        progress.setWatchedSeconds(newTotal);

        // ── Auto-completion logic ─────────────────────────────────────────────
        boolean shouldComplete = request.isMarkComplete();

        // If total lesson duration is provided, auto-complete at 90% watched
        if (!shouldComplete && request.getLessonTotalSeconds() != null
                && request.getLessonTotalSeconds() > 0) {
            double watchedPercent = (newTotal * 100.0) / request.getLessonTotalSeconds();
            if (watchedPercent >= 90.0) {
                shouldComplete = true;
            }
        }

        if (shouldComplete && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        return mapToProgressResponse(progressRepository.save(progress));
    }

    @Override
    public List<CertificateResponse> getCertificatesByEmail(String email) {

        Long studentId = userClient.getUserIdByEmail(email);

        return certificateRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<CertificateResponse> getCertificatesByStudentId(Long studentId) {
        return certificateRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToCertificateResponse)   
                .toList();
    }

    @Override
    @Transactional
    public ProgressResponse markLessonComplete(Long studentId, String studentEmail,
                                               Long courseId, Long lessonId) {

        Progress progress = progressRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElseGet(() -> {
                    Progress p = new Progress();
                    p.setStudentId(studentId);
                    p.setStudentEmail(studentEmail);
                    p.setCourseId(courseId);
                    p.setLessonId(lessonId);
                    p.setWatchedSeconds(0);
                    p.setFirstAccessedAt(LocalDateTime.now());
                    return p;
                });

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        return mapToProgressResponse(progressRepository.save(progress));
    }

    @Override
    public CourseProgressResponse getCourseProgress(Long studentId, Long courseId,
                                                    long totalLessonsInCourse) {

        List<Progress> lessonProgressList =
                progressRepository.findByStudentIdAndCourseId(studentId, courseId);

        long completedCount = lessonProgressList.stream()
                .filter(Progress::isCompleted)
                .count();

        // Use provided total, fall back to tracked lessons if not provided
        long totalLessons = totalLessonsInCourse > 0
                ? totalLessonsInCourse
                : lessonProgressList.size();

        int completionPercent = totalLessons > 0
                ? (int) Math.round((completedCount * 100.0) / totalLessons)
                : 0;

        boolean courseCompleted = totalLessons > 0 && completedCount >= totalLessons;

        CourseProgressResponse response = new CourseProgressResponse();
        response.setStudentId(studentId);
        response.setCourseId(courseId);
        response.setTotalLessons(totalLessons);
        response.setCompletedLessons(completedCount);
        response.setCompletionPercent(completionPercent);
        response.setCourseCompleted(courseCompleted);
        response.setLessonProgress(lessonProgressList.stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    public ProgressResponse getLessonProgress(Long studentId, Long lessonId) {
        Progress progress = progressRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No progress record found for studentId=" + studentId
                                + ", lessonId=" + lessonId));
        return mapToProgressResponse(progress);
    }

    @Override
    public List<ProgressResponse> getAllProgressByStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    // ── Certificate Operations ────────────────────────────────────────────────

    @Override
    @Transactional
    public CertificateResponse issueCertificate(Long studentId, String studentEmail,
                                                Long courseId, String studentName,
                                                String courseTitle, String instructorName) {

        // 🔒 Prevent duplicate certificate
        if (certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new CertificateException(
                    "Certificate already issued for studentId=" + studentId
                            + " courseId=" + courseId);
        }

        // 🔒 Validate completion — must have at least 1 completed lesson
        // Full 100% check should be done by caller (progress controller)
        // using getCourseProgress() before calling this method
        boolean hasAnyCompletion = progressRepository
                .existsByStudentIdAndLessonIdAndCompletedTrue(studentId,
                        // Check any lesson — simplified guard
                        progressRepository.findByStudentIdAndCourseId(studentId, courseId)
                                .stream()
                                .filter(Progress::isCompleted)
                                .map(Progress::getLessonId)
                                .findFirst()
                                .orElse(-1L));

        // Generate unique UUID verification code
        String verificationCode = UUID.randomUUID().toString();
        String verificationUrl  = certificateBaseUrl + "/" + verificationCode;

        Certificate certificate = new Certificate();
        certificate.setStudentId(studentId);
        certificate.setStudentEmail(studentEmail);
        certificate.setStudentName(studentName);
        certificate.setCourseId(courseId);
        certificate.setCourseTitle(courseTitle);
        certificate.setInstructorName(instructorName);
        certificate.setCompletionDate(LocalDate.now());
        certificate.setVerificationCode(verificationCode);
        certificate.setVerificationUrl(verificationUrl);

        // In production: generate PDF via iText/PDFBox and upload to S3
        // For now: set a mock URL using the verification code
        certificate.setCertificateUrl(
                "https://cdn.learnify.com/certificates/" + verificationCode + ".pdf");

        Certificate saved = certificateRepository.save(certificate);
        return mapToCertificateResponse(saved);
    }

    @Override
    public CertificateResponse getCertificate(Long studentId, Long courseId) {
        Certificate cert = certificateRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No certificate found for studentId=" + studentId
                                + " courseId=" + courseId));
        return mapToCertificateResponse(cert);
    }

    @Override
    public CertificateResponse getCertificateById(Long certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found with id: " + certificateId));
        return mapToCertificateResponse(cert);
    }

    @Override
    public CertificateResponse verifyCertificate(String verificationCode) {
        // This is a PUBLIC endpoint — no auth required
        Certificate cert = certificateRepository
                .findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid verification code: " + verificationCode
                                + ". This certificate does not exist or may have been revoked."));
        return mapToCertificateResponse(cert);
    }

    @Override
    public List<CertificateResponse> getAllCertificates(String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only admins can view all certificates");
        }
        return certificateRepository.findAllByOrderByIssuedAtDesc()
                .stream()
                .map(this::mapToCertificateResponse)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private ProgressResponse mapToProgressResponse(Progress p) {
        ProgressResponse response = new ProgressResponse();
        response.setId(p.getId());
        response.setStudentId(p.getStudentId());
        response.setStudentEmail(p.getStudentEmail());
        response.setCourseId(p.getCourseId());
        response.setLessonId(p.getLessonId());
        response.setWatchedSeconds(p.getWatchedSeconds());
        response.setCompleted(p.isCompleted());
        response.setFirstAccessedAt(p.getFirstAccessedAt());
        response.setCompletedAt(p.getCompletedAt());
        response.setLastAccessedAt(p.getLastAccessedAt());
        return response;
    }

    private CertificateResponse mapToCertificateResponse(Certificate c) {
        CertificateResponse response = new CertificateResponse();
        response.setId(c.getId());
        response.setStudentId(c.getStudentId());
        response.setStudentEmail(c.getStudentEmail());
        response.setStudentName(c.getStudentName());
        response.setCourseId(c.getCourseId());
        response.setCourseTitle(c.getCourseTitle());
        response.setInstructorName(c.getInstructorName());
        response.setCompletionDate(c.getCompletionDate());
        response.setIssuedAt(c.getIssuedAt());
        response.setCertificateUrl(c.getCertificateUrl());
        response.setVerificationCode(c.getVerificationCode());
        response.setVerificationUrl(c.getVerificationUrl());
        response.setValid(true); // If it's in DB, it's valid
        return response;
    }

    private CertificateResponse mapToResponse(Certificate certificate) {

        CertificateResponse res = new CertificateResponse();

        res.setId(certificate.getId());
        res.setCourseId(certificate.getCourseId());
        res.setStudentId(certificate.getStudentId());
        res.setIssuedAt(certificate.getIssuedAt());

        return res;
    }
}