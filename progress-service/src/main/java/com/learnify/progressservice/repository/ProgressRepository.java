package com.learnify.progressservice.repository;

import com.learnify.progressservice.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Find progress for a specific student + lesson (unique)
    Optional<Progress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    // All lesson progress records for a student in a course
    List<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // All progress records for a student (across all courses)
    List<Progress> findByStudentId(Long studentId);

    // Count completed lessons for a student in a course
    long countByStudentIdAndCourseIdAndCompletedTrue(Long studentId, Long courseId);

    // All completed lesson records for a student in a course
    List<Progress> findByStudentIdAndCourseIdAndCompletedTrue(Long studentId, Long courseId);

    // Check if a lesson is already completed by student
    boolean existsByStudentIdAndLessonIdAndCompletedTrue(Long studentId, Long lessonId);

    // Count distinct lessons tracked for a student in a course (started)
    long countByStudentIdAndCourseId(Long studentId, Long courseId);

    // Total watch time for a student across all lessons in a course (seconds)
    @Query("SELECT COALESCE(SUM(p.watchedSeconds), 0) FROM Progress p " +
            "WHERE p.studentId = :studentId AND p.courseId = :courseId")
    Integer sumWatchedSecondsByCourse(@Param("studentId") Long studentId,
                                      @Param("courseId")  Long courseId);
}