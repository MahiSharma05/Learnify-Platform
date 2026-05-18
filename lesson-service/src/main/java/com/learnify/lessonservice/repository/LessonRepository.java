package com.learnify.lessonservice.repository;

import com.learnify.lessonservice.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // All lessons for a course, sorted by orderIndex ascending
    List<Lesson> findByCourseIdOrderByOrderIndex(Long courseId);

    // Only preview lessons for a course (for guest/unenrolled access)
    List<Lesson> findByCourseIdAndIsPreviewTrueOrderByOrderIndex(Long courseId);

    // Filter by content type within a course
    List<Lesson> findByCourseIdAndContentType(Long courseId, String contentType);

    // Count lessons in a course (used by progress-service)
    long countByCourseId(Long courseId);

    // Check if a lesson belongs to a course
    boolean existsByIdAndCourseId(Long lessonId, Long courseId);

    // Find the highest orderIndex for a course (for auto-appending)
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.courseId = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);
}