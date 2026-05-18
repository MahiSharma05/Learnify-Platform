package com.learnify.progressservice.repository;

import com.learnify.progressservice.entity.Progress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)

@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})

class ProgressRepositoryTest {

    @Autowired
    private ProgressRepository progressRepository;

    // =========================================================
    // TEST 1
    // Save Progress
    // =========================================================

    @Test
    @DisplayName("Save Progress")
    void saveProgress_ShouldWork() {

        Progress progress = new Progress();

        progress.setStudentId(1L);
        progress.setCourseId(1L);
        progress.setLessonId(10L);
        progress.setWatchedSeconds(200);
        progress.setCompleted(false);

        Progress saved = progressRepository.save(progress);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(200, saved.getWatchedSeconds());
    }

    // =========================================================
    // TEST 2
    // Find By StudentId And LessonId
    // =========================================================

    @Test
    @DisplayName("Find By StudentId And LessonId")
    void findByStudentIdAndLessonId_ShouldReturnProgress() {

        Progress progress = new Progress();

        progress.setStudentId(1L);
        progress.setCourseId(1L);
        progress.setLessonId(5L);
        progress.setWatchedSeconds(100);

        progressRepository.save(progress);

        Optional<Progress> result =
                progressRepository.findByStudentIdAndLessonId(1L, 5L);

        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getLessonId());
    }

    // =========================================================
    // TEST 3
    // Find By StudentId
    // =========================================================

    @Test
    @DisplayName("Find By StudentId")
    void findByStudentId_ShouldReturnList() {

        Progress p1 = new Progress();
        p1.setStudentId(1L);
        p1.setCourseId(1L);
        p1.setLessonId(1L);
        p1.setWatchedSeconds(100);

        Progress p2 = new Progress();
        p2.setStudentId(1L);
        p2.setCourseId(1L);
        p2.setLessonId(2L);
        p2.setWatchedSeconds(200);

        progressRepository.save(p1);
        progressRepository.save(p2);

        List<Progress> result =
                progressRepository.findByStudentId(1L);

        assertEquals(2, result.size());
    }

    // =========================================================
    // TEST 4
    // Find By StudentId And CourseId
    // =========================================================

    @Test
    @DisplayName("Find By StudentId And CourseId")
    void findByStudentIdAndCourseId_ShouldReturnList() {

        Progress progress = new Progress();

        progress.setStudentId(1L);
        progress.setCourseId(2L);
        progress.setLessonId(10L);
        progress.setWatchedSeconds(300);

        progressRepository.save(progress);

        List<Progress> result =
                progressRepository.findByStudentIdAndCourseId(1L, 2L);

        assertEquals(1, result.size());
    }

    // =========================================================
    // TEST 5
    // Count By StudentId And CourseId
    // =========================================================

    @Test
    @DisplayName("Count By StudentId And CourseId")
    void countByStudentIdAndCourseId_ShouldReturnCorrectCount() {

        Progress p1 = new Progress();
        p1.setStudentId(1L);
        p1.setCourseId(1L);
        p1.setLessonId(1L);
        p1.setWatchedSeconds(100);

        Progress p2 = new Progress();
        p2.setStudentId(1L);
        p2.setCourseId(1L);
        p2.setLessonId(2L);
        p2.setWatchedSeconds(200);

        progressRepository.save(p1);
        progressRepository.save(p2);

        long count =
                progressRepository.countByStudentIdAndCourseId(1L, 1L);

        assertEquals(2, count);
    }

    // =========================================================
    // TEST 6
    // Exists Completed
    // =========================================================

    @Test
    @DisplayName("Exists Completed")
    void existsByStudentIdAndLessonIdAndCompletedTrue_ShouldReturnTrue() {

        Progress progress = new Progress();

        progress.setStudentId(1L);
        progress.setCourseId(1L);
        progress.setLessonId(11L);
        progress.setWatchedSeconds(500);
        progress.setCompleted(true);

        progressRepository.save(progress);

        boolean exists =
                progressRepository
                        .existsByStudentIdAndLessonIdAndCompletedTrue(1L, 11L);

        assertTrue(exists);
    }

    // =========================================================
    // TEST 7
    // Count Completed
    // =========================================================

    @Test
    @DisplayName("Count Completed")
    void countByStudentIdAndCourseIdAndCompletedTrue_ShouldReturnCount() {

        Progress progress = new Progress();

        progress.setStudentId(1L);
        progress.setCourseId(1L);
        progress.setLessonId(20L);
        progress.setWatchedSeconds(1000);
        progress.setCompleted(true);

        progressRepository.save(progress);

        long count =
                progressRepository
                        .countByStudentIdAndCourseIdAndCompletedTrue(1L, 1L);

        assertEquals(1, count);
    }

    // =========================================================
    // TEST 8
    // Sum Watched Seconds
    // =========================================================

    @Test
    @DisplayName("Sum Watched Seconds")
    void sumWatchedSecondsByCourse_ShouldReturnTotal() {

        Progress p1 = new Progress();
        p1.setStudentId(1L);
        p1.setCourseId(1L);
        p1.setLessonId(1L);
        p1.setWatchedSeconds(100);

        Progress p2 = new Progress();
        p2.setStudentId(1L);
        p2.setCourseId(1L);
        p2.setLessonId(2L);
        p2.setWatchedSeconds(200);

        progressRepository.save(p1);
        progressRepository.save(p2);

        Integer total =
                progressRepository.sumWatchedSecondsByCourse(1L, 1L);

        assertEquals(300, total);
    }

    // =========================================================
    // TEST 9
    // Completed Lesson List
    // =========================================================

    @Test
    @DisplayName("Completed Lesson List")
    void findByStudentIdAndCourseIdAndCompletedTrue_ShouldReturnList() {

        Progress progress = new Progress();

        progress.setStudentId(1L);
        progress.setCourseId(1L);
        progress.setLessonId(50L);
        progress.setWatchedSeconds(600);
        progress.setCompleted(true);

        progressRepository.save(progress);

        List<Progress> result =
                progressRepository
                        .findByStudentIdAndCourseIdAndCompletedTrue(1L, 1L);

        assertEquals(1, result.size());
    }
}