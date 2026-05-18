package com.learnify.assessmentservice.entity;

import com.learnify.assessmentservice.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Quiz
    @Column(nullable = false)
    private Long quizId;

    // The question text
    @Column(nullable = false, length = 2000)
    private String text;

    // MCQ, MULTI, TRUE_FALSE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    /**
     * List of answer options stored as a JSON array in the DB.
     * For TRUE_FALSE: options = ["True", "False"]
     * For MCQ/MULTI: options = ["Option A", "Option B", "Option C", "Option D"]
     */
    @ElementCollection
    @CollectionTable(
            name = "question_options",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "option_text")
    private List<String> options;

    /**
     * Correct answer(s) stored as comma-separated string.
     * For MCQ/TRUE_FALSE: "True" or "Option A"
     * For MULTI: "Option A,Option C"
     */
    @Column(nullable = false, length = 500)
    private String correctAnswer;

    // Points this question is worth
    @Column(nullable = false)
    private Integer marks = 1;

    // Display order within the quiz (1-based)
    @Column(nullable = false)
    private Integer orderIndex;
}