package com.learnify.assessmentservice.repository;

import com.learnify.assessmentservice.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // All quizzes for a course
    List<Quiz> findByCourseId(Long courseId);

    // Only published quizzes (visible to students)
    List<Quiz> findByCourseIdAndPublishedTrue(Long courseId);

    // Quizzes linked to a specific lesson
    List<Quiz> findByLessonId(Long lessonId);

    // Count quizzes in a course
    long countByCourseId(Long courseId);
}