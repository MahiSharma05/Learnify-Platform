package com.learnify.lessonservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.lessonservice.dto.ResourceRequest;
import com.learnify.lessonservice.dto.ResourceResponse;
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

@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAddResource() throws Exception {

        ResourceRequest request = new ResourceRequest();
        request.setName("PDF");
        request.setFileUrl("url");

        when(lessonService.addResource(anyLong(), any(), anyString(), anyString()))
                .thenReturn(new ResourceResponse());

        mockMvc.perform(post("/api/lessons/1/resources")
                        .header("X-User-Email", "a")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetResources() throws Exception {

        when(lessonService.getResourcesByLesson(1L))
                .thenReturn(List.of(new ResourceResponse()));

        mockMvc.perform(get("/api/lessons/1/resources"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteResource() throws Exception {

        doNothing().when(lessonService)
                .removeResource(anyLong(), anyLong(), anyString(), anyString());

        mockMvc.perform(delete("/api/lessons/1/resources/1")
                        .header("X-User-Email", "a")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }
}