package com.learnify.assessmentservice.service;

import com.learnify.assessmentservice.dto.*;
import com.learnify.assessmentservice.entity.Attempt;
import com.learnify.assessmentservice.entity.Question;
import com.learnify.assessmentservice.entity.Quiz;
import com.learnify.assessmentservice.enums.QuestionType;
import com.learnify.assessmentservice.exception.AttemptLimitExceededException;
import com.learnify.assessmentservice.exception.ResourceNotFoundException;
import com.learnify.assessmentservice.exception.UnauthorizedException;
import com.learnify.assessmentservice.repository.AttemptRepository;
import com.learnify.assessmentservice.repository.QuestionRepository;
import com.learnify.assessmentservice.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;

    // Constructor injection — consistent with project pattern
    public QuizServiceImpl(QuizRepository quizRepository,
                           QuestionRepository questionRepository,
                           AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    // ── Quiz CRUD ────────────────────────────────────────────────────────────

    @Override
    public QuizResponse createQuiz(QuizRequest request,
                                   String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can create quizzes");
        }

        Quiz quiz = new Quiz();
        quiz.setCourseId(request.getCourseId());
        quiz.setLessonId(request.getLessonId());
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setPublished(false);
        quiz.setCreatedByEmail(userEmail);

        return mapQuizToResponse(quizRepository.save(quiz), List.of());
    }

    @Override
    public QuizResponse getQuizById(Long quizId, String userRole) {
        Quiz quiz = findQuizOrThrow(quizId);

        // Students can only view published quizzes
        if (!quiz.isPublished() && !isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("This quiz is not yet published");
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        return mapQuizToResponse(quiz, questions);
    }

    @Override
    public List<QuizResponse> getQuizzesByCourse(Long courseId, String userRole) {
        List<Quiz> quizzes;

        // Instructors/admins see all quizzes; students see only published ones
        if (isInstructorOrAdmin(userRole)) {
            quizzes = quizRepository.findByCourseId(courseId);
        } else {
            quizzes = quizRepository.findByCourseIdAndPublishedTrue(courseId);
        }

        return quizzes.stream()
                .map(quiz -> {
                    List<Question> questions =
                            questionRepository.findByQuizIdOrderByOrderIndex(quiz.getId());
                    return mapQuizToResponse(quiz, questions);
                })
                .collect(Collectors.toList());
    }

    @Override
    public QuizResponse updateQuiz(Long quizId, QuizRequest request,
                                   String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can update quizzes");
        }

        Quiz quiz = findQuizOrThrow(quizId);
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setMaxAttempts(request.getMaxAttempts());

        List<Question> questions =
                questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        return mapQuizToResponse(quizRepository.save(quiz), questions);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId, String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can delete quizzes");
        }

        findQuizOrThrow(quizId);
        // Cascade delete questions first (no orphan records)
        questionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }

    @Override
    public QuizResponse publishQuiz(Long quizId, String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can publish quizzes");
        }

        Quiz quiz = findQuizOrThrow(quizId);

        // Must have at least 1 question before publishing
        long questionCount = questionRepository.countByQuizId(quizId);
        if (questionCount == 0) {
            throw new UnauthorizedException(
                    "Cannot publish a quiz with no questions. Add at least one question first.");
        }

        quiz.setPublished(true);
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
        return mapQuizToResponse(quizRepository.save(quiz), questions);
    }

    // ── Question Management ──────────────────────────────────────────────────

    @Override
    public QuestionResponse addQuestion(Long quizId, QuestionRequest request,
                                        String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can add questions");
        }

        findQuizOrThrow(quizId);

        Question question = new Question();
        question.setQuizId(quizId);
        question.setText(request.getText());
        question.setType(QuestionType.valueOf(request.getType().toUpperCase()));
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setMarks(request.getMarks());
        question.setOrderIndex(request.getOrderIndex());

        return mapQuestionToResponse(questionRepository.save(question));
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId, String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can delete questions");
        }

        questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + questionId));

        questionRepository.deleteById(questionId);
    }

    @Override
    public List<QuestionResponse> getQuestionsByQuiz(Long quizId) {
        findQuizOrThrow(quizId);
        return questionRepository.findByQuizIdOrderByOrderIndex(quizId)
                .stream()
                .map(this::mapQuestionToResponse)
                .collect(Collectors.toList());
    }

    // ── Attempt Lifecycle ────────────────────────────────────────────────────

    @Override
    @Transactional
    public AttemptStartResponse startAttempt(Long quizId,
                                             Long studentId,
                                             String studentEmail) {

        Quiz quiz = findQuizOrThrow(quizId);

        // 🔒 Quiz must be published for students to attempt
        if (!quiz.isPublished()) {
            throw new UnauthorizedException("This quiz is not yet published");
        }

        // 🔒 Check max attempts limit
        if (quiz.getMaxAttempts() > 0) {
            long attemptsTaken = attemptRepository.countByStudentIdAndQuizId(studentId, quizId);
            if (attemptsTaken >= quiz.getMaxAttempts()) {
                throw new AttemptLimitExceededException(
                        "You have reached the maximum attempts (" +
                                quiz.getMaxAttempts() + ") for this quiz");
            }
        }

        // Create the attempt record
        Attempt attempt = new Attempt();
        attempt.setQuizId(quizId);
        attempt.setStudentId(studentId);
        attempt.setStudentEmail(studentEmail);
        attempt.setStartedAt(LocalDateTime.now());

        Attempt saved = attemptRepository.save(attempt);

        // Fetch questions (WITHOUT correct answers in response)
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);

        // Build response
        AttemptStartResponse response = new AttemptStartResponse();
        response.setAttemptId(saved.getId());
        response.setQuizId(quizId);
        response.setQuizTitle(quiz.getTitle());
        response.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        response.setStartedAt(saved.getStartedAt());

        // Set expiry if time-limited
        if (quiz.getTimeLimitMinutes() > 0) {
            response.setExpiresAt(
                    saved.getStartedAt().plusMinutes(quiz.getTimeLimitMinutes()));
        }

        response.setQuestions(questions.stream()
                .map(this::mapQuestionToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public AttemptResponse submitAttempt(Long attemptId,
                                         AttemptSubmitRequest request,
                                         Long studentId,
                                         String studentEmail) {

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found with id: " + attemptId));

        // 🔒 Only the student who started can submit
        if (!attempt.getStudentId().equals(studentId)) {
            throw new UnauthorizedException("You can only submit your own attempt");
        }

        // 🔒 Cannot resubmit
        if (attempt.getSubmittedAt() != null) {
            throw new UnauthorizedException("This attempt has already been submitted");
        }

        Quiz quiz = findQuizOrThrow(attempt.getQuizId());

        // ⏰ Check if time limit was exceeded (if applicable)
        if (quiz.getTimeLimitMinutes() > 0) {
            LocalDateTime expiry =
                    attempt.getStartedAt().plusMinutes(quiz.getTimeLimitMinutes());
            // We still grade it — just log that it was late (could add flag later)
        }

        // 🧮 AUTO-GRADING ENGINE
        List<Question> questions =
                questionRepository.findByQuizIdOrderByOrderIndex(attempt.getQuizId());

        int totalMarks    = 0;
        int marksObtained = 0;
        List<AttemptResponse.QuestionResult> questionResults = new ArrayList<>();

        Map<Long, String> studentAnswers = request.getAnswers();

        for (Question question : questions) {
            totalMarks += question.getMarks();

            String studentAnswer  =
                    studentAnswers.getOrDefault(question.getId(), "").trim();
            String correctAnswer  = question.getCorrectAnswer().trim();

            boolean isCorrect = gradeAnswer(question.getType(),
                    studentAnswer, correctAnswer);
            int marksAwarded = isCorrect ? question.getMarks() : 0;
            marksObtained += marksAwarded;

            // Build per-question result (reveals correct answer after submission)
            AttemptResponse.QuestionResult result = new AttemptResponse.QuestionResult();
            result.setQuestionId(question.getId());
            result.setQuestionText(question.getText());
            result.setStudentAnswer(studentAnswer);
            result.setCorrectAnswer(correctAnswer);  // ← revealed now
            result.setCorrect(isCorrect);
            result.setMarksAwarded(marksAwarded);
            result.setMarksTotal(question.getMarks());
            questionResults.add(result);
        }

        // Calculate percentage score
        int scorePercent = totalMarks > 0
                ? (int) Math.round((marksObtained * 100.0) / totalMarks)
                : 0;

        boolean passed = scorePercent >= quiz.getPassingScore();

        // Persist graded attempt
        attempt.setAnswers(request.getAnswers());
        attempt.setMarksObtained(marksObtained);
        attempt.setTotalMarks(totalMarks);
        attempt.setScore(scorePercent);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(LocalDateTime.now());

        Attempt graded = attemptRepository.save(attempt);

        return buildAttemptResponse(graded, quiz, questionResults);
    }

    @Override
    public List<AttemptResponse> getAttemptsByStudent(Long studentId) {
        return attemptRepository.findByStudentId(studentId)
                .stream()
                .map(a -> {
                    Quiz quiz = quizRepository.findById(a.getQuizId()).orElse(null);
                    return buildAttemptResponse(a, quiz, List.of());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AttemptResponse> getAttemptsByQuiz(Long quizId, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException(
                    "Only instructors or admins can view all attempts for a quiz");
        }

        Quiz quiz = findQuizOrThrow(quizId);
        return attemptRepository.findByQuizId(quizId)
                .stream()
                .map(a -> buildAttemptResponse(a, quiz, List.of()))
                .collect(Collectors.toList());
    }

    @Override
    public AttemptResponse getBestAttempt(Long quizId, Long studentId) {
        Quiz quiz = findQuizOrThrow(quizId);
        Attempt best = attemptRepository
                .findBestAttemptByStudentIdAndQuizId(studentId, quizId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No attempts found for student " + studentId +
                                " on quiz " + quizId));
        return buildAttemptResponse(best, quiz, List.of());
    }

    @Override
    public AttemptResponse getAttemptById(Long attemptId,
                                          Long studentId, String userRole) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found with id: " + attemptId));

        // 🔒 Student can only view own attempts; instructors/admins see all
        if (!attempt.getStudentId().equals(studentId) && !isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Access denied for this attempt");
        }

        Quiz quiz = findQuizOrThrow(attempt.getQuizId());
        return buildAttemptResponse(attempt, quiz, List.of());
    }

    // ── Auto-Grading Logic ───────────────────────────────────────────────────

    /**
     * Grades a single answer based on question type.
     *
     * MCQ / TRUE_FALSE: exact match (case-insensitive)
     * MULTI: both sets must match exactly (order-independent)
     */
    private boolean gradeAnswer(QuestionType type,
                                String studentAnswer,
                                String correctAnswer) {

        if (studentAnswer == null || studentAnswer.isBlank()) return false;

        if (type == QuestionType.MULTI) {
            // Split comma-separated values, sort, compare sets
            Set<String> studentSet = Arrays.stream(studentAnswer.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            Set<String> correctSet = Arrays.stream(correctAnswer.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            return studentSet.equals(correctSet);
        }

        // MCQ and TRUE_FALSE: simple case-insensitive match
        return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    // ── Private Mapping Helpers ──────────────────────────────────────────────

    private Quiz findQuizOrThrow(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz not found with id: " + quizId));
    }

    private boolean isInstructorOrAdmin(String role) {
        return "INSTRUCTOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    // Map Quiz + Questions → QuizResponse
    // NOTE: For student-facing calls, correct answers are hidden via QuestionResponse DTO
    private QuizResponse mapQuizToResponse(Quiz quiz, List<Question> questions) {
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setCourseId(quiz.getCourseId());
        response.setLessonId(quiz.getLessonId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        response.setPassingScore(quiz.getPassingScore());
        response.setMaxAttempts(quiz.getMaxAttempts());
        response.setPublished(quiz.isPublished());
        response.setCreatedByEmail(quiz.getCreatedByEmail());
        response.setCreatedAt(quiz.getCreatedAt());
        response.setUpdatedAt(quiz.getUpdatedAt());
        response.setQuestions(questions.stream()
                .map(this::mapQuestionToResponse)
                .collect(Collectors.toList()));
        return response;
    }

    // Map Question → QuestionResponse (NO correct answer field)
    private QuestionResponse mapQuestionToResponse(Question q) {
        QuestionResponse response = new QuestionResponse();
        response.setId(q.getId());
        response.setQuizId(q.getQuizId());
        response.setText(q.getText());
        response.setType(q.getType().name());
        response.setOptions(q.getOptions());
        response.setMarks(q.getMarks());
        response.setOrderIndex(q.getOrderIndex());
        // ✅ correctAnswer intentionally NOT set here
        return response;
    }

    // Build full AttemptResponse from graded attempt
    private AttemptResponse buildAttemptResponse(Attempt attempt, Quiz quiz,
                                                 List<AttemptResponse.QuestionResult> questionResults) {
        AttemptResponse response = new AttemptResponse();
        response.setId(attempt.getId());
        response.setQuizId(attempt.getQuizId());
        response.setQuizTitle(quiz != null ? quiz.getTitle() : null);
        response.setStudentId(attempt.getStudentId());
        response.setStudentEmail(attempt.getStudentEmail());
        response.setScore(attempt.getScore());
        response.setMarksObtained(attempt.getMarksObtained());
        response.setTotalMarks(attempt.getTotalMarks());
        response.setPassed(attempt.isPassed());
        response.setPassingScore(quiz != null ? quiz.getPassingScore() : null);
        response.setStartedAt(attempt.getStartedAt());
        response.setSubmittedAt(attempt.getSubmittedAt());
        response.setAnswers(attempt.getAnswers());
        response.setQuestionResults(questionResults);
        return response;
    }
}