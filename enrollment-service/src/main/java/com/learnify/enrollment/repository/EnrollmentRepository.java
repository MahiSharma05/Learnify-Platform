package com.learnify.enrollment.repository;

import com.learnify.enrollment.entity.Enrollment;
import com.learnify.enrollment.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // All enrollments for a student
    List<Enrollment> findByStudentId(Long studentId);

    // All enrollments for a course (used by instructors & admins)
    List<Enrollment> findByCourseId(Long courseId);

    // Find a specific student-course enrollment
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Check if a student is enrolled in a course (used for access gating)
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // Filter by status (ACTIVE, COMPLETED, CANCELLED)
    List<Enrollment> findByStatus(EnrollmentStatus status);

    // Count total enrollments for a course (analytics)
    long countByCourseId(Long courseId);

    // Count active enrollments for a course
    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    // All completed enrollments for a student (for certificate listing)
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);
}