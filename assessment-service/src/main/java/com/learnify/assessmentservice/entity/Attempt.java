package com.learnify.assessmentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "attempts")
@Data
@NoArgsConstructor
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quizId;

    // Student who took this attempt
    @Column(nullable = false)
    private Long studentId;

    private String studentEmail;

    // Score achieved (percentage 0-100)
    private Integer score = 0;

    // true if score >= quiz.passingScore
    private boolean passed = false;

    // When the student started the quiz
    private LocalDateTime startedAt;

    // When answers were submitted (or timer expired)
    private LocalDateTime submittedAt;

    /**
     * Student's answers: Map<questionId, studentAnswer>
     * e.g. { 1: "True", 2: "Option A", 3: "Option B,Option D" }
     * Stored as JSON string in DB using @Convert
     */
    @ElementCollection
    @CollectionTable(
            name = "attempt_answers",
            joinColumns = @JoinColumn(name = "attempt_id")
    )
    @MapKeyColumn(name = "question_id")
    @Column(name = "student_answer", length = 500)
    private Map<Long, String> answers;

    // Total marks earned (raw score before percentage conversion)
    private Integer marksObtained = 0;

    // Total marks possible for this quiz
    private Integer totalMarks = 0;
}