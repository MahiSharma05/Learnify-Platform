package com.learnify.courseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.courseservice.dto.CourseRequest;
import com.learnify.courseservice.entity.Course;
import com.learnify.courseservice.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course course;
    private CourseRequest request;

    @BeforeEach
    void setup() {

        course = new Course();
        course.setId(1L);
        course.setTitle("Java Masterclass");
        course.setCategory("Programming");

        request = new CourseRequest();
        request.setTitle("Java Masterclass");
        request.setCategory("Programming");
    }

    @Test
    @DisplayName("Create course success")
    void createCourseSuccess() throws Exception {

        Mockito.when(courseService.createCourse(any(), anyString(), anyString()))
                .thenReturn(course);

        mockMvc.perform(post("/api/courses")
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "INSTRUCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Masterclass"));
    }

    @Test
    @DisplayName("Create course validation fail")
    void createCourseValidationFail() throws Exception {

        request.setTitle("");

        mockMvc.perform(post("/api/courses")
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "INSTRUCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCourses() throws Exception {

        Mockito.when(courseService.getAllPublishedCourses())
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Masterclass"));
    }

    @Test
    void getCourseById() throws Exception {

        Mockito.when(courseService.getCourseById(1L))
                .thenReturn(course);

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCourseByIdNotFound() throws Exception {

        Mockito.when(courseService.getCourseById(1L))
                .thenThrow(new RuntimeException("Course not found"));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchCourses() throws Exception {

        Mockito.when(courseService.searchCourses("java"))
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses/search")
                        .param("keyword", "java"))
                .andExpect(status().isOk());
    }

    @Test
    void filterByCategory() throws Exception {

        Mockito.when(courseService.filterByCategory("Programming"))
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses/filter")
                        .param("category", "Programming"))
                .andExpect(status().isOk());
    }

    @Test
    void filterByLevel() throws Exception {

        Mockito.when(courseService.filterByLevel("Beginner"))
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses/filter")
                        .param("level", "Beginner"))
                .andExpect(status().isOk());
    }

    @Test
    void publishCourse() throws Exception {

        mockMvc.perform(put("/api/courses/1/publish")
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "INSTRUCTOR"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCourse() throws Exception {

        mockMvc.perform(delete("/api/courses/1")
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }
}