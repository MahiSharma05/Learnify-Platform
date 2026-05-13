package com.learnify.assessmentservice.service;

import com.learnify.assessmentservice.dto.*;

import java.util.List;

public interface QuizService {

    // ── Quiz CRUD ────────────────────────────────────────────────────────────

    QuizResponse createQuiz(QuizRequest request, String userEmail, String userRole);

    QuizResponse getQuizById(Long quizId, String userRole);

    List<QuizResponse> getQuizzesByCourse(Long courseId, String userRole);

    QuizResponse updateQuiz(Long quizId, QuizRequest request,
                            String userEmail, String userRole);

    void deleteQuiz(Long quizId, String userEmail, String userRole);

    QuizResponse publishQuiz(Long quizId, String userEmail, String userRole);

    // ── Question Management ──────────────────────────────────────────────────

    QuestionResponse addQuestion(Long quizId, QuestionRequest request,
                                 String userEmail, String userRole);

    void deleteQuestion(Long questionId, String userEmail, String userRole);

    List<QuestionResponse> getQuestionsByQuiz(Long quizId);

    // ── Attempt Lifecycle ────────────────────────────────────────────────────

    // Start a timed attempt — returns questions WITHOUT correct answers
    AttemptStartResponse startAttempt(Long quizId, Long studentId, String studentEmail);

    // Submit answers — auto-grades and returns full result
    AttemptResponse submitAttempt(Long attemptId, AttemptSubmitRequest request,
                                  Long studentId, String studentEmail);

    // Get all attempts by the logged-in student
    List<AttemptResponse> getAttemptsByStudent(Long studentId);

    // Get all attempts for a specific quiz (instructor/admin)
    List<AttemptResponse> getAttemptsByQuiz(Long quizId, String userRole);

    // Get a student's best attempt for a quiz
    AttemptResponse getBestAttempt(Long quizId, Long studentId);

    // Get a single attempt by ID
    AttemptResponse getAttemptById(Long attemptId, Long studentId, String userRole);
}