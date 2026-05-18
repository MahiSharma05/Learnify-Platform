package com.learnify.assessmentservice.controller;

import com.learnify.assessmentservice.dto.AttemptResponse;
import com.learnify.assessmentservice.dto.AttemptStartResponse;
import com.learnify.assessmentservice.dto.AttemptSubmitRequest;
import com.learnify.assessmentservice.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final QuizService quizService;

    public AttemptController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * POST /api/attempts/start/{quizId}
     * Start a timed quiz attempt.
     * Returns questions WITHOUT correct answers.
     * Roles: STUDENT
     */
    @PostMapping("/start/{quizId}")
    public ResponseEntity<AttemptStartResponse> startAttempt(
            @PathVariable Long quizId,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String studentEmail) {

        return ResponseEntity.status(201)
                .body(quizService.startAttempt(quizId, studentId, studentEmail));
    }

    /**
     * POST /api/attempts/{id}/submit
     * Submit answers for an attempt — triggers auto-grading.
     * Returns full result with correct answers revealed.
     * Roles: STUDENT (own attempt only)
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<AttemptResponse> submitAttempt(
            @PathVariable Long id,
            @Valid @RequestBody AttemptSubmitRequest request,
            @RequestHeader("X-User-Id")    Long studentId,
            @RequestHeader("X-User-Email") String studentEmail) {

        return ResponseEntity.ok(
                quizService.submitAttempt(id, request, studentId, studentEmail));
    }

    /**
     * GET /api/attempts/my
     * Get all attempts by the logged-in student.
     * Roles: STUDENT
     */
    @GetMapping("/my")
    public ResponseEntity<List<AttemptResponse>> getMyAttempts(
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(quizService.getAttemptsByStudent(studentId));
    }

    /**
     * GET /api/attempts/quiz/{quizId}
     * Get all attempts for a quiz.
     * Roles: INSTRUCTOR, ADMIN
     */
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByQuiz(
            @PathVariable Long quizId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(quizService.getAttemptsByQuiz(quizId, role));
    }

    /**
     * GET /api/attempts/quiz/{quizId}/best
     * Get the best (highest scoring) attempt by the student for a quiz.
     * Roles: STUDENT
     */
    @GetMapping("/quiz/{quizId}/best")
    public ResponseEntity<AttemptResponse> getBestAttempt(
            @PathVariable Long quizId,
            @RequestHeader("X-User-Id") Long studentId) {

        return ResponseEntity.ok(quizService.getBestAttempt(quizId, studentId));
    }

    /**
     * GET /api/attempts/{id}
     * Get a single attempt by ID.
     * Roles: STUDENT (own), INSTRUCTOR, ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttemptResponse> getAttemptById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   Long studentId,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(quizService.getAttemptById(id, studentId, role));
    }
}