package com.learnify.assessmentservice.controller;
import com.learnify.assessmentservice.dto.QuizTimerState;
import com.learnify.assessmentservice.service.QuizTimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * QuizTimerController — NEW FILE
 *
 * Exposes REST endpoints for the frontend to poll remaining quiz time
 * and save in-progress answers mid-quiz (auto-save).
 *
 * Base path: /api/quiz-timer
 *
 * Endpoints:
 *   GET  /api/quiz-timer/{attemptId}/remaining  → remaining seconds + isExpired flag
 *   POST /api/quiz-timer/{attemptId}/answers    → save in-progress answers to Redis
 *   GET  /api/quiz-timer/{attemptId}/state      → full timer state (debug/admin use)
 */
@RestController
@RequestMapping("/api/quiz-timer")
@RequiredArgsConstructor
@Slf4j
public class QuizTimerController {
    private final QuizTimerService quizTimerService;

    // =========================================================================
    // GET /api/quiz-timer/{attemptId}/remaining
    // Called by frontend every ~5 seconds to update the countdown display.
    // =========================================================================

    /**
     * Returns remaining seconds and expiry flag for a quiz attempt.
     *
     * Response:
     * {
     *   "attemptId": 42,
     *   "remainingSeconds": 287,
     *   "isExpired": false
     * }
     */
    @GetMapping("/{attemptId}/remaining")
    public ResponseEntity<Map<String, Object>> getRemainingTime(
            @PathVariable Long attemptId) {

        long remaining = quizTimerService.getRemainingSeconds(attemptId);
        boolean expired = quizTimerService.isExpired(attemptId);

        Map<String, Object> response = new HashMap<>();
        response.put("attemptId", attemptId);
        response.put("remainingSeconds", remaining);
        response.put("isExpired", expired);

        log.debug("[QuizTimerController] Remaining for attemptId={}: {}s expired={}",
                attemptId, remaining, expired);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // POST /api/quiz-timer/{attemptId}/answers
    // Called by frontend to auto-save in-progress answers every ~30 seconds.
    // Ensures answers are not lost if the timer expires before manual submit.
    // =========================================================================

    /**
     * Saves current in-progress answers to Redis.
     *
     * Request body: { "1": "A", "2": "True", "3": "B" }
     *   Key   = questionId (as String, parsed to Long)
     *   Value = selected answer string
     *
     * Response:
     * {
     *   "saved": true,
     *   "remainingSeconds": 250
     * }
     */
    @PostMapping("/{attemptId}/answers")
    public ResponseEntity<Map<String, Object>> saveAnswers(
            @PathVariable Long attemptId,
            @RequestBody Map<String, String> rawAnswers) {

        // Convert String keys to Long keys
        Map<Long, String> answers = new HashMap<>();
        rawAnswers.forEach((k, v) -> answers.put(Long.parseLong(k), v));

        // Guard: do not save if already expired
        if (quizTimerService.isExpired(attemptId)) {
            Map<String, Object> expired = new HashMap<>();
            expired.put("saved", false);
            expired.put("isExpired", true);
            expired.put("remainingSeconds", 0);
            return ResponseEntity.ok(expired);
        }

        quizTimerService.updateAnswers(attemptId, answers);

        Map<String, Object> response = new HashMap<>();
        response.put("saved", true);
        response.put("isExpired", false);
        response.put("remainingSeconds", quizTimerService.getRemainingSeconds(attemptId));

        log.debug("[QuizTimerController] Answers saved for attemptId={} count={}",
                attemptId, answers.size());

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // GET /api/quiz-timer/{attemptId}/state
    // Full timer state — useful for debugging and admin inspection.
    // =========================================================================

    /**
     * Returns the full QuizTimerState stored in Redis.
     * Returns 404 if the key does not exist (expired or never started).
     */
    @GetMapping("/{attemptId}/state")
    public ResponseEntity<?> getTimerState(@PathVariable Long attemptId) {

        Optional<QuizTimerState> stateOpt = quizTimerService.getTimer(attemptId);

        if (stateOpt.isEmpty()) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("error", "Timer state not found or expired for attemptId: " + attemptId);
            return ResponseEntity.status(404).body(notFound);
        }

        return ResponseEntity.ok(stateOpt.get());
    }
}
