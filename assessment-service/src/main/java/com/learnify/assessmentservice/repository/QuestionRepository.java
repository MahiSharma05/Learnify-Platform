package com.learnify.assessmentservice.repository;

import com.learnify.assessmentservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // All questions for a quiz ordered by position
    List<Question> findByQuizIdOrderByOrderIndex(Long quizId);

    // Count questions in a quiz
    long countByQuizId(Long quizId);

    // Delete all questions for a quiz (when quiz is deleted)
    void deleteByQuizId(Long quizId);
}