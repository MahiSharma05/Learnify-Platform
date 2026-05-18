package com.learnify.assessmentservice.repository;

import com.learnify.assessmentservice.entity.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QuizRepositoryTest {

    @Autowired
    private QuizRepository quizRepository;

    @Test
    @DisplayName("Should save quiz successfully")
    void shouldSaveQuiz() {

        Quiz quiz = new Quiz();
        quiz.setCourseId(1L);
        quiz.setTitle("Java Quiz");
        quiz.setDescription("Basic Java");
        quiz.setPublished(true);

        Quiz saved = quizRepository.save(quiz);

        assertNotNull(saved.getId());
        assertEquals("Java Quiz", saved.getTitle());
    }

    @Test
    @DisplayName("Should find published quizzes by course")
    void shouldFindPublishedQuizzes() {

        Quiz quiz = new Quiz();
        quiz.setCourseId(10L);
        quiz.setTitle("Spring");
        quiz.setPublished(true);

        quizRepository.save(quiz);

        List<Quiz> quizzes =
                quizRepository.findByCourseIdAndPublishedTrue(10L);

        assertEquals(1, quizzes.size());
    }
}