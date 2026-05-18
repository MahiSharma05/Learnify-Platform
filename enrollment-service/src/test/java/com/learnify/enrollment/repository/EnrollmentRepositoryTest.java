package com.learnify.enrollment.repository;

import com.learnify.enrollment.entity.Enrollment;
import com.learnify.enrollment.enums.EnrollmentStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // =====================================
    // TEST 1 : FIND BY STUDENT ID
    // =====================================
    @Test
    void shouldFindEnrollmentsByStudentId() {

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(1L);
        enrollment.setCourseId(101L);
        enrollment.setStudentEmail("mahi@gmail.com");
        enrollment.setCourseTitle("Java");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        enrollmentRepository.save(enrollment);

        List<Enrollment> result =
                enrollmentRepository.findByStudentId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getStudentId());
    }

    // =====================================
    // TEST 2 : CHECK ENROLLMENT EXISTS
    // =====================================
    @Test
    void shouldCheckEnrollmentExists() {

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(1L);
        enrollment.setCourseId(101L);
        enrollment.setStudentEmail("mahi@gmail.com");
        enrollment.setCourseTitle("Java");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        enrollmentRepository.save(enrollment);

        boolean exists =
                enrollmentRepository
                        .existsByStudentIdAndCourseId(1L, 101L);

        assertTrue(exists);
    }

    // =====================================
    // TEST 3 : FIND BY COURSE ID
    // =====================================
    @Test
    void shouldFindByCourseId() {

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(1L);
        enrollment.setCourseId(101L);
        enrollment.setStudentEmail("mahi@gmail.com");
        enrollment.setCourseTitle("Java");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        enrollmentRepository.save(enrollment);

        List<Enrollment> result =
                enrollmentRepository.findByCourseId(101L);

        assertEquals(1, result.size());
    }

    // =====================================
    // TEST 4 : COUNT ENROLLMENTS
    // =====================================
    @Test
    void shouldCountEnrollmentsByCourseId() {

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(1L);
        enrollment.setCourseId(101L);
        enrollment.setStudentEmail("mahi@gmail.com");
        enrollment.setCourseTitle("Java");
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        enrollmentRepository.save(enrollment);

        long count =
                enrollmentRepository.countByCourseId(101L);

        assertEquals(1, count);
    }
}