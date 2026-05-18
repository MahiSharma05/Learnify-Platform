package com.learnify.courseservice.repository;

import com.learnify.courseservice.entity.Course;
import com.learnify.courseservice.enums.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository repository;

    private Course course;

    @BeforeEach
    void setup() {

        course = new Course();
        course.setTitle("Java Course");
        course.setDescription("Spring Boot");
        course.setCategory("Programming");
        course.setLevel("Beginner");
        course.setInstructorId(1L);
        course.setInstructorName("test@gmail.com");
        course.setPublished(true);
        course.setFeatured(true);
        course.setPrice(100.0);
        course.setApprovalStatus(ApprovalStatus.APPROVED);

        repository.save(course);
    }

    @Test
    void saveCourse() {

        assertNotNull(course.getId());
    }

    @Test
    void findByPublishedTrue() {

        List<Course> courses = repository.findByPublishedTrue();

        assertFalse(courses.isEmpty());
    }

    @Test
    void searchByKeyword() {

        List<Course> courses = repository.searchByKeyword("Java");

        assertEquals(1, courses.size());
    }

    @Test
    void findByInstructorId() {

        List<Course> courses = repository.findByInstructorId(1L);

        assertEquals(1, courses.size());
    }

    @Test
    void findByCategoryAndPublishedTrue() {

        List<Course> courses =
                repository.findByCategoryAndPublishedTrue("Programming");

        assertEquals(1, courses.size());
    }
}