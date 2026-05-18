package com.learnify.courseservice.service;

import com.learnify.courseservice.dto.CourseRequest;
import com.learnify.courseservice.entity.Course;
import com.learnify.courseservice.enums.ApprovalStatus;
import com.learnify.courseservice.repository.CourseRepository;
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
class CourseServiceImplTest {

    @Mock
    private CourseRepository repository;

    @InjectMocks
    private CourseServiceImpl service;

    private Course course;
    private CourseRequest request;

    @BeforeEach
    void setup() {

        course = new Course();
        course.setId(1L);
        course.setTitle("Spring Boot");
        course.setInstructorName("test@gmail.com");

        request = new CourseRequest();
        request.setTitle("Spring Boot");
        request.setCategory("Programming");
    }

    @Test
    void createCourseSuccess() {

        when(repository.save(any())).thenReturn(course);

        Course saved = service.createCourse(
                request,
                "test@gmail.com",
                "INSTRUCTOR"
        );

        assertNotNull(saved);
        verify(repository, times(1)).save(any());
    }

    @Test
    void createCourseInvalidRole() {

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createCourse(request, "x@gmail.com", "STUDENT"));

        assertEquals("Only instructors can create courses", ex.getMessage());
    }

    @Test
    void getCourseByIdSuccess() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(course));

        Course result = service.getCourseById(1L);

        assertEquals("Spring Boot", result.getTitle());
    }

    @Test
    void getCourseByIdFail() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getCourseById(1L));
    }

    @Test
    void getAllPublishedCourses() {

        when(repository.findByPublishedTrue())
                .thenReturn(List.of(course));

        List<Course> result = service.getAllPublishedCourses();

        assertEquals(1, result.size());
    }

    @Test
    void searchCourses() {

        when(repository.searchByKeyword("java"))
                .thenReturn(List.of(course));

        List<Course> result = service.searchCourses("java");

        assertFalse(result.isEmpty());
    }

    @Test
    void filterByCategory() {

        when(repository.findByCategoryAndPublishedTrue("Programming"))
                .thenReturn(List.of(course));

        List<Course> result = service.filterByCategory("Programming");

        assertEquals(1, result.size());
    }

    @Test
    void updateCourseSuccess() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(course));

        when(repository.save(any())).thenReturn(course);

        Course updated = service.updateCourse(
                1L,
                request,
                "test@gmail.com",
                "INSTRUCTOR"
        );

        assertNotNull(updated);
    }

    @Test
    void updateCourseUnauthorized() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(course));

        assertThrows(RuntimeException.class,
                () -> service.updateCourse(
                        1L,
                        request,
                        "abc@gmail.com",
                        "INSTRUCTOR"
                ));
    }

    @Test
    void publishCourse() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(course));

        service.publishCourse(1L);

        assertEquals(ApprovalStatus.PENDING, course.getApprovalStatus());
    }

    @Test
    void approveCourse() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(course));

        service.approveCourse(1L);

        assertTrue(course.isPublished());
    }

    @Test
    void deleteCourse() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(course));

        service.deleteCourse(
                1L,
                "test@gmail.com",
                "INSTRUCTOR"
        );

        verify(repository, times(1)).delete(course);
    }
}