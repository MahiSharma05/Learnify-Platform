package com.learnify.assessmentservice.service;
import com.learnify.assessmentservice.dto.QuizTimerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * QuizTimerService — NEW FILE
 *
 * Manages quiz attempt timer state in Redis.
 *
 * Key format : quiz:timer:<attemptId>
 * TTL        : quiz timeLimitSeconds + graceSeconds (from yml)
 *
 * Used by: AssessmentServiceImpl (startAttempt, submitAttempt, updateAnswers)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizTimerService {

    private static final String TIMER_KEY_PREFIX = "quiz:timer:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${quiz.timer.grace-seconds:60}")
    private int graceSeconds;

    // ── Start Timer ─────────────────────────────────────────────────────────

    /**
     * Called when a student starts a quiz attempt.
     * Stores the timer state in Redis with TTL = timeLimitSeconds + graceSeconds.
     *
     * @param attemptId       DB ID of the newly created Attempt
     * @param quizId          ID of the Quiz
     * @param studentId       ID of the Student
     * @param timeLimitSeconds Time limit configured on the Quiz
     */
    public QuizTimerState startTimer(Long attemptId, Long quizId,
                                     Long studentId, int timeLimitSeconds) {
        String key = buildKey(attemptId);

        // Prevent double-start: check if timer already exists
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            log.warn("[QuizTimer] Timer already exists for attemptId={}", attemptId);
            return getTimer(attemptId)
                    .orElseThrow(() -> new IllegalStateException("Timer state missing"));
        }

        QuizTimerState state = QuizTimerState.builder()
                .attemptId(attemptId)
                .quizId(quizId)
                .studentId(studentId)
                .startedAt(LocalDateTime.now())
                .timeLimitSeconds(timeLimitSeconds)
                .currentAnswers(new java.util.HashMap<>())
                .autoSubmitted(false)
                .build();

        // TTL = quiz time limit + grace period
        Duration ttl = Duration.ofSeconds(timeLimitSeconds + graceSeconds);
        redisTemplate.opsForValue().set(key, state, ttl);

        log.info("[QuizTimer] Timer started — attemptId={} quizId={} student={} limit={}s",
                attemptId, quizId, studentId, timeLimitSeconds);
        return state;
    }

    // ── Get Timer State ──────────────────────────────────────────────────────

    /**
     * Retrieves the current timer state for an attempt.
     * Returns Optional.empty() if the key has expired or never existed.
     */
    public Optional<QuizTimerState> getTimer(Long attemptId) {
        String key = buildKey(attemptId);
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw instanceof QuizTimerState state) {
            return Optional.of(state);
        }
        return Optional.empty();
    }

    // ── Update In-Progress Answers ───────────────────────────────────────────

    /**
     * Saves the student's current answers mid-quiz (auto-save).
     * Preserves the original TTL so saving answers doesn't extend the timer.
     *
     * @param attemptId ID of the attempt
     * @param answers   Map of questionId → selectedAnswer
     */
    public void updateAnswers(Long attemptId, Map<Long, String> answers) {
        String key = buildKey(attemptId);
        Optional<QuizTimerState> stateOpt = getTimer(attemptId);

        if (stateOpt.isEmpty()) {
            log.warn("[QuizTimer] Cannot update answers — timer not found for attemptId={}", attemptId);
            return;
        }

        QuizTimerState state = stateOpt.get();

        // Guard: do not update if already expired
        if (state.isExpired()) {
            log.warn("[QuizTimer] Cannot update answers — timer expired for attemptId={}", attemptId);
            return;
        }

        state.setCurrentAnswers(answers);

        // Preserve remaining TTL
        Long remainingMs = redisTemplate.getExpire(key,
                java.util.concurrent.TimeUnit.MILLISECONDS);
        if (remainingMs != null && remainingMs > 0) {
            redisTemplate.opsForValue().set(key, state, Duration.ofMillis(remainingMs));
        }

        log.debug("[QuizTimer] Answers updated — attemptId={} answers={}", attemptId, answers.size());
    }

    // ── Check If Expired ─────────────────────────────────────────────────────

    /**
     * Returns true if the quiz timer has expired for the given attempt.
     * Also returns true if the key doesn't exist (expired from Redis TTL).
     */
    public boolean isExpired(Long attemptId) {
        Optional<QuizTimerState> stateOpt = getTimer(attemptId);
        if (stateOpt.isEmpty()) {
            // Key doesn't exist → Redis TTL expired → quiz is auto-expired
            return true;
        }
        return stateOpt.get().isExpired();
    }

    // ── Get Remaining Seconds ────────────────────────────────────────────────

    /**
     * Returns remaining quiz seconds. Returns 0 if expired or not found.
     */
    public long getRemainingSeconds(Long attemptId) {
        return getTimer(attemptId)
                .map(QuizTimerState::getRemainingSeconds)
                .orElse(0L);
    }

    // ── Mark Auto-Submitted ─────────────────────────────────────────────────

    /**
     * Marks the timer state as auto-submitted (prevents duplicate submission).
     */
    public void markAutoSubmitted(Long attemptId) {
        String key = buildKey(attemptId);
        Optional<QuizTimerState> stateOpt = getTimer(attemptId);
        stateOpt.ifPresent(state -> {
            state.setAutoSubmitted(true);
            // Keep for 5 minutes after auto-submit (for audit/debug)
            redisTemplate.opsForValue().set(key, state, Duration.ofMinutes(5));
            log.info("[QuizTimer] Marked auto-submitted — attemptId={}", attemptId);
        });
    }

    // ── Delete Timer ─────────────────────────────────────────────────────────

    /**
     * Explicitly deletes the timer state after successful submission.
     * Called by AssessmentServiceImpl.submitAttempt() after DB save.
     */
    public void deleteTimer(Long attemptId) {
        String key = buildKey(attemptId);
        Boolean deleted = redisTemplate.delete(key);
        log.info("[QuizTimer] Timer deleted — attemptId={} wasPresent={}", attemptId, deleted);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private String buildKey(Long attemptId) {
        return TIMER_KEY_PREFIX + attemptId;
    }
}
