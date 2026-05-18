package com.learnify.lessonservice.service;

import com.learnify.lessonservice.dto.*;
import com.learnify.lessonservice.entity.Lesson;
import com.learnify.lessonservice.entity.LessonResource;
import com.learnify.lessonservice.exception.ResourceNotFoundException;
import com.learnify.lessonservice.exception.UnauthorizedException;
import com.learnify.lessonservice.repository.LessonRepository;
import com.learnify.lessonservice.repository.LessonResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonResourceRepository lessonResourceRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Lesson lesson;

    @BeforeEach
    void setup() {
        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCourseId(1L);
        lesson.setTitle("Java");
        lesson.setContentType("VIDEO");
        lesson.setOrderIndex(1);
    }

    @Test
    void shouldAddLessonSuccessfully() {
        LessonRequest request = new LessonRequest();
        request.setCourseId(1L);
        request.setTitle("Java");
        request.setContentType("VIDEO");
        request.setOrderIndex(1);

        when(lessonRepository.save(any())).thenReturn(lesson);

        LessonResponse response =
                lessonService.addLesson(request, "a@gmail.com", "INSTRUCTOR");

        assertEquals("Java", response.getTitle());
    }

    @Test
    void shouldThrowUnauthorizedWhenAddingLesson() {
        LessonRequest request = new LessonRequest();

        assertThrows(UnauthorizedException.class,
                () -> lessonService.addLesson(request, "a", "STUDENT"));
    }

    @Test
    void shouldGetLessonById() {

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        when(lessonResourceRepository.findByLessonId(1L))
                .thenReturn(List.of());

        LessonResponse response = lessonService.getLessonById(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    void shouldThrowWhenLessonNotFound() {

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> lessonService.getLessonById(1L));
    }

    @Test
    void shouldGetLessonsByCourse() {

        when(lessonRepository.findByCourseIdOrderByOrderIndex(1L))
                .thenReturn(List.of(lesson));

        when(lessonResourceRepository.findByLessonId(1L))
                .thenReturn(List.of());

        List<LessonResponse> responses =
                lessonService.getLessonsByCourse(1L);

        assertEquals(1, responses.size());
    }

    @Test
    void shouldUpdateLessonSuccessfully() {

        LessonRequest request = new LessonRequest();
        request.setTitle("Updated");
        request.setContentType("VIDEO");
        request.setOrderIndex(1);

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        when(lessonRepository.save(any()))
                .thenReturn(lesson);

        when(lessonResourceRepository.findByLessonId(1L))
                .thenReturn(List.of());

        LessonResponse response =
                lessonService.updateLesson(1L, request, "a", "ADMIN");

        assertNotNull(response);
    }

    @Test
    void shouldThrowUnauthorizedWhenUpdating() {

        LessonRequest request = new LessonRequest();

        assertThrows(UnauthorizedException.class,
                () -> lessonService.updateLesson(1L, request, "a", "USER"));
    }

    @Test
    void shouldDeleteLessonSuccessfully() {

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        lessonService.deleteLesson(1L, "a", "ADMIN");

        verify(lessonResourceRepository).deleteByLessonId(1L);
        verify(lessonRepository).delete(lesson);
    }

    @Test
    void shouldThrowUnauthorizedWhenDeleting() {

        assertThrows(UnauthorizedException.class,
                () -> lessonService.deleteLesson(1L, "a", "USER"));
    }

    @Test
    void shouldReorderLessonsSuccessfully() {

        ReorderRequest request = new ReorderRequest();
        request.setCourseId(1L);
        request.setLessonIds(List.of(1L));

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        lessonService.reorderLessons(request, "a", "ADMIN");

        verify(lessonRepository).save(any());
    }

    @Test
    void shouldThrowWhenLessonDoesNotBelongToCourse() {

        lesson.setCourseId(2L);

        ReorderRequest request = new ReorderRequest();
        request.setCourseId(1L);
        request.setLessonIds(List.of(1L));

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        assertThrows(UnauthorizedException.class,
                () -> lessonService.reorderLessons(request, "a", "ADMIN"));
    }

    @Test
    void shouldGetPreviewLessons() {

        lesson.setPreview(true);

        when(lessonRepository
                .findByCourseIdAndIsPreviewTrueOrderByOrderIndex(1L))
                .thenReturn(List.of(lesson));

        when(lessonResourceRepository.findByLessonId(1L))
                .thenReturn(List.of());

        List<LessonResponse> responses =
                lessonService.getPreviewLessons(1L);

        assertEquals(1, responses.size());
    }

    @Test
    void shouldAddResourceSuccessfully() {

        ResourceRequest request = new ResourceRequest();
        request.setName("PDF");
        request.setFileUrl("url");

        LessonResource resource = new LessonResource();
        resource.setId(1L);

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        when(lessonResourceRepository.save(any()))
                .thenReturn(resource);

        ResourceResponse response =
                lessonService.addResource(1L, request, "a", "ADMIN");

        assertNotNull(response);
    }

    @Test
    void shouldThrowUnauthorizedWhenAddingResource() {

        ResourceRequest request = new ResourceRequest();

        assertThrows(UnauthorizedException.class,
                () -> lessonService.addResource(1L, request, "a", "USER"));
    }

    @Test
    void shouldRemoveResourceSuccessfully() {

        LessonResource resource = new LessonResource();
        resource.setId(1L);
        resource.setLessonId(1L);

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        when(lessonResourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        lessonService.removeResource(1L, 1L, "a", "ADMIN");

        verify(lessonResourceRepository).delete(resource);
    }

    @Test
    void shouldThrowWhenResourceNotBelongingToLesson() {

        LessonResource resource = new LessonResource();
        resource.setLessonId(2L);

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        when(lessonResourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        assertThrows(UnauthorizedException.class,
                () -> lessonService.removeResource(1L, 1L, "a", "ADMIN"));
    }

    @Test
    void shouldGetResourcesByLesson() {

        LessonResource resource = new LessonResource();
        resource.setId(1L);

        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        when(lessonResourceRepository.findByLessonId(1L))
                .thenReturn(List.of(resource));

        List<ResourceResponse> responses =
                lessonService.getResourcesByLesson(1L);

        assertEquals(1, responses.size());
    }
}