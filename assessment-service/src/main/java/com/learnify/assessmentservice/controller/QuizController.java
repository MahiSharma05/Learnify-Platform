package com.learnify.assessmentservice.controller;

import com.learnify.assessmentservice.dto.*;
import com.learnify.assessmentservice.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * POST /api/quizzes
     * Create a new quiz for a course.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(
            @Valid @RequestBody QuizRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.status(201)
                .body(quizService.createQuiz(request, email, role));
    }

    /**
     * GET /api/quizzes/{id}
     * Get quiz by ID.
     * Roles: STUDENT (published only), INSTRUCTOR, ADMIN (all)
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuizById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "STUDENT") String role) {

        return ResponseEntity.ok(quizService.getQuizById(id, role));
    }

    /**
     * GET /api/quizzes/course/{courseId}
     * Get all quizzes for a course.
     * Roles: STUDENT (published only), INSTRUCTOR/ADMIN (all)
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-User-Role", defaultValue = "STUDENT") String role) {

        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId, role));
    }

    /**
     * PUT /api/quizzes/{id}
     * Update quiz details.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(quizService.updateQuiz(id, request, email, role));
    }

    /**
     * DELETE /api/quizzes/{id}
     * Delete a quiz and all its questions.
     * Roles: INSTRUCTOR, ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuiz(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        quizService.deleteQuiz(id, email, role);
        return ResponseEntity.ok("Quiz deleted successfully");
    }

    /**
     * PUT /api/quizzes/{id}/publish
     * Publish a quiz (make it visible to students).
     * Roles: INSTRUCTOR, ADMIN
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<QuizResponse> publishQuiz(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(quizService.publishQuiz(id, email, role));
    }

    // ── Question Management ──────────────────────────────────────────────────

    /**
     * POST /api/quizzes/{quizId}/questions
     * Add a question to a quiz.
     * Roles: INSTRUCTOR, ADMIN
     */
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.status(201)
                .body(quizService.addQuestion(quizId, request, email, role));
    }

    /**
     * GET /api/quizzes/{quizId}/questions
     * Get all questions for a quiz.
     * Roles: STUDENT (no correct answers), INSTRUCTOR/ADMIN
     */
    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @PathVariable Long quizId) {

        return ResponseEntity.ok(quizService.getQuestionsByQuiz(quizId));
    }

    /**
     * DELETE /api/quizzes/questions/{questionId}
     * Delete a single question.
     * Roles: INSTRUCTOR, ADMIN
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<String> deleteQuestion(
            @PathVariable Long questionId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        quizService.deleteQuestion(questionId, email, role);
        return ResponseEntity.ok("Question deleted successfully");
    }
}