package com.learnify.assessmentservice.repository;

import com.learnify.assessmentservice.entity.Attempt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AttemptRepositoryTest {

    @Autowired
    private AttemptRepository attemptRepository;

    @Test
    @DisplayName("Should save attempt successfully")
    void shouldSaveAttempt() {

        Attempt attempt = new Attempt();
        attempt.setQuizId(1L);
        attempt.setStudentId(100L);
        attempt.setStartedAt(LocalDateTime.now());

        Attempt saved = attemptRepository.save(attempt);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getQuizId());
        assertEquals(100L, saved.getStudentId());
    }

    @Test
    @DisplayName("Should count attempts by studentId and quizId")
    void shouldCountAttempts() {

        Attempt attempt = new Attempt();
        attempt.setQuizId(1L);
        attempt.setStudentId(10L);

        attemptRepository.save(attempt);

        long count =
                attemptRepository.countByStudentIdAndQuizId(10L, 1L);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should find attempt by id")
    void shouldFindAttemptById() {

        Attempt attempt = new Attempt();
        attempt.setQuizId(2L);
        attempt.setStudentId(200L);

        Attempt saved = attemptRepository.save(attempt);

        Optional<Attempt> found =
                attemptRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(200L, found.get().getStudentId());
    }

    @Test
    @DisplayName("Should return empty when id not found")
    void shouldReturnEmptyWhenIdNotFound() {

        Optional<Attempt> found =
                attemptRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should update attempt")
    void shouldUpdateAttempt() {

        Attempt attempt = new Attempt();
        attempt.setQuizId(3L);
        attempt.setStudentId(300L);

        Attempt saved = attemptRepository.save(attempt);

        saved.setStudentId(999L);

        Attempt updated = attemptRepository.save(saved);

        assertEquals(999L, updated.getStudentId());
    }

    @Test
    @DisplayName("Should delete attempt")
    void shouldDeleteAttempt() {

        Attempt attempt = new Attempt();
        attempt.setQuizId(4L);
        attempt.setStudentId(400L);

        Attempt saved = attemptRepository.save(attempt);

        attemptRepository.deleteById(saved.getId());

        Optional<Attempt> deleted =
                attemptRepository.findById(saved.getId());

        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Should return all attempts")
    void shouldReturnAllAttempts() {

        Attempt attempt1 = new Attempt();
        attempt1.setQuizId(5L);
        attempt1.setStudentId(500L);

        Attempt attempt2 = new Attempt();
        attempt2.setQuizId(6L);
        attempt2.setStudentId(600L);

        attemptRepository.save(attempt1);
        attemptRepository.save(attempt2);

        List<Attempt> attempts =
                attemptRepository.findAll();

        assertTrue(attempts.size() >= 2);
    }

    @Test
    @DisplayName("Should count multiple attempts correctly")
    void shouldCountMultipleAttemptsCorrectly() {

        Attempt attempt1 = new Attempt();
        attempt1.setQuizId(10L);
        attempt1.setStudentId(1000L);

        Attempt attempt2 = new Attempt();
        attempt2.setQuizId(10L);
        attempt2.setStudentId(1000L);

        attemptRepository.save(attempt1);
        attemptRepository.save(attempt2);

        long count =
                attemptRepository.countByStudentIdAndQuizId(1000L, 10L);

        assertEquals(2, count);
    }
}