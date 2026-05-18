package com.learnify.assessmentservice.repository;

import com.learnify.assessmentservice.entity.Question;
import com.learnify.assessmentservice.enums.QuestionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void shouldFindQuestionsByQuizId() {

        Question q = new Question();
        q.setQuizId(1L);
        q.setText("What is Java?");
        q.setType(QuestionType.MCQ);
        q.setCorrectAnswer("A");
        q.setMarks(1);
        q.setOrderIndex(1);

        questionRepository.save(q);

        List<Question> questions =
                questionRepository.findByQuizIdOrderByOrderIndex(1L);

        assertFalse(questions.isEmpty());
    }
}