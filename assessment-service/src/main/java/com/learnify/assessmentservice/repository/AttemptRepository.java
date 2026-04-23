package com.learnify.assessmentservice.repository;

import com.learnify.assessmentservice.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    // All attempts by a student
    List<Attempt> findByStudentId(Long studentId);

    // All attempts for a specific quiz
    List<Attempt> findByQuizId(Long quizId);

    // All attempts by a student for a specific quiz
    List<Attempt> findByStudentIdAndQuizId(Long studentId, Long quizId);

    // Count how many times a student has attempted a quiz
    long countByStudentIdAndQuizId(Long studentId, Long quizId);

    // Get best (highest) score for a student on a quiz
    @Query("SELECT a FROM Attempt a WHERE a.studentId = :studentId " +
            "AND a.quizId = :quizId ORDER BY a.score DESC LIMIT 1")
    Optional<Attempt> findBestAttemptByStudentIdAndQuizId(
            @Param("studentId") Long studentId,
            @Param("quizId") Long quizId);

    // All passed attempts by a student (for analytics)
    List<Attempt> findByStudentIdAndPassedTrue(Long studentId);
}