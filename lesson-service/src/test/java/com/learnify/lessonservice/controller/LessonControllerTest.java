package com.learnify.lessonservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.lessonservice.dto.LessonRequest;
import com.learnify.lessonservice.dto.LessonResponse;
import com.learnify.lessonservice.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
@AutoConfigureMockMvc(addFilters = false)
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAddLesson() throws Exception {

        LessonRequest request = new LessonRequest();
        request.setCourseId(1L);
        request.setTitle("Java");
        request.setContentType("VIDEO");
        request.setOrderIndex(1);

        LessonResponse response = new LessonResponse();
        response.setTitle("Java");

        when(lessonService.addLesson(any(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/lessons")
                        .header("X-User-Email", "a")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetLessonsByCourse() throws Exception {

        when(lessonService.getLessonsByCourse(1L))
                .thenReturn(List.of(new LessonResponse()));

        mockMvc.perform(get("/api/lessons/course/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetLessonById() throws Exception {

        when(lessonService.getLessonById(1L))
                .thenReturn(new LessonResponse());

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateLesson() throws Exception {

        LessonRequest request = new LessonRequest();
        request.setCourseId(1L);
        request.setTitle("Java");
        request.setContentType("VIDEO");
        request.setOrderIndex(1);

        when(lessonService.updateLesson(anyLong(), any(), anyString(), anyString()))
                .thenReturn(new LessonResponse());

        mockMvc.perform(put("/api/lessons/1")
                        .header("X-User-Email", "a")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteLesson() throws Exception {

        doNothing().when(lessonService)
                .deleteLesson(anyLong(), anyString(), anyString());

        mockMvc.perform(delete("/api/lessons/1")
                        .header("X-User-Email", "a")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPreviewLessons() throws Exception {

        when(lessonService.getPreviewLessons(1L))
                .thenReturn(List.of(new LessonResponse()));

        mockMvc.perform(get("/api/lessons/course/1/preview"))
                .andExpect(status().isOk());
    }
}