package com.learnify.lessonservice.repository;

import com.learnify.lessonservice.entity.Lesson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LessonRepositoryTest {

    @Autowired
    private LessonRepository lessonRepository;

    private Lesson createLesson(Long courseId,
                                String title,
                                String type,
                                Integer orderIndex,
                                boolean preview) {

        Lesson lesson = new Lesson();
        lesson.setCourseId(courseId);
        lesson.setTitle(title);
        lesson.setContentType(type);
        lesson.setOrderIndex(orderIndex);
        lesson.setPreview(preview);

        return lessonRepository.save(lesson);
    }

    @Test
    @DisplayName("Should find lessons by course id ordered by order index")
    void shouldFindLessonsByCourseIdOrderByOrderIndex() {

        createLesson(1L, "Lesson 2", "VIDEO", 2, false);
        createLesson(1L, "Lesson 1", "PDF", 1, false);

        List<Lesson> lessons =
                lessonRepository.findByCourseIdOrderByOrderIndex(1L);

        assertEquals(2, lessons.size());
        assertEquals(1, lessons.get(0).getOrderIndex());
        assertEquals(2, lessons.get(1).getOrderIndex());
    }

    @Test
    @DisplayName("Should find preview lessons only")
    void shouldFindPreviewLessons() {

        createLesson(1L, "Preview", "VIDEO", 1, true);
        createLesson(1L, "Private", "PDF", 2, false);

        List<Lesson> lessons =
                lessonRepository
                        .findByCourseIdAndIsPreviewTrueOrderByOrderIndex(1L);

        assertEquals(1, lessons.size());
        assertTrue(lessons.get(0).isPreview());
    }

    @Test
    @DisplayName("Should find lessons by content type")
    void shouldFindLessonsByContentType() {

        createLesson(1L, "Video", "VIDEO", 1, false);
        createLesson(1L, "Pdf", "PDF", 2, false);

        List<Lesson> lessons =
                lessonRepository.findByCourseIdAndContentType(1L, "VIDEO");

        assertEquals(1, lessons.size());
        assertEquals("VIDEO", lessons.get(0).getContentType());
    }

    @Test
    @DisplayName("Should count lessons by course id")
    void shouldCountLessonsByCourseId() {

        createLesson(1L, "Java", "VIDEO", 1, false);
        createLesson(1L, "Spring", "PDF", 2, false);

        long count = lessonRepository.countByCourseId(1L);

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should check lesson exists by id and course id")
    void shouldCheckLessonExistsByIdAndCourseId() {

        Lesson lesson =
                createLesson(1L, "Java", "VIDEO", 1, false);

        boolean exists =
                lessonRepository.existsByIdAndCourseId(
                        lesson.getId(),
                        1L
                );

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when lesson does not belong to course")
    void shouldReturnFalseWhenLessonNotBelongingToCourse() {

        Lesson lesson =
                createLesson(1L, "Java", "VIDEO", 1, false);

        boolean exists =
                lessonRepository.existsByIdAndCourseId(
                        lesson.getId(),
                        2L
                );

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should return max order index")
    void shouldReturnMaxOrderIndex() {

        createLesson(1L, "One", "VIDEO", 1, false);
        createLesson(1L, "Two", "VIDEO", 5, false);

        Integer max =
                lessonRepository.findMaxOrderIndexByCourseId(1L);

        assertEquals(5, max);
    }

    @Test
    @DisplayName("Should return zero when no lessons exist")
    void shouldReturnZeroWhenNoLessonsExist() {

        Integer max =
                lessonRepository.findMaxOrderIndexByCourseId(99L);

        assertEquals(0, max);
    }

    @Test
    @DisplayName("Should return empty list for unknown course")
    void shouldReturnEmptyListForUnknownCourse() {

        List<Lesson> lessons =
                lessonRepository.findByCourseIdOrderByOrderIndex(999L);

        assertTrue(lessons.isEmpty());
    }
}