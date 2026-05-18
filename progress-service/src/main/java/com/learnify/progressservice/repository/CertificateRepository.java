package com.learnify.progressservice.repository;

import com.learnify.progressservice.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // All certificates earned by a student
    List<Certificate> findByStudentId(Long studentId);

    // Certificate for a specific student + course
    Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Find by verification code — used for public verification endpoint
    Optional<Certificate> findByVerificationCode(String verificationCode);

    // Check if certificate already exists for student + course
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // All certificates issued (Admin view)
    List<Certificate> findAllByOrderByIssuedAtDesc();

    // Count certificates by course (analytics)
    long countByCourseId(Long courseId);
}