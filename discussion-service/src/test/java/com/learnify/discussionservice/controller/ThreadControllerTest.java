package com.learnify.discussionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.discussionservice.dto.ThreadRequest;
import com.learnify.discussionservice.dto.ThreadResponse;
import com.learnify.discussionservice.exception.ResourceNotFoundException;
import com.learnify.discussionservice.exception.UnauthorizedException;
import com.learnify.discussionservice.service.DiscussionService;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThreadController.class)
@AutoConfigureMockMvc(addFilters = false)
class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscussionService discussionService;

    @Autowired
    private ObjectMapper objectMapper;

    // ----------------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------------

    private ThreadRequest createRequest() {
        ThreadRequest request = new ThreadRequest();
        request.setCourseId(1L);
        request.setLessonId(10L);
        request.setTitle("Java Thread");
        request.setBody("Thread Body");
        request.setAuthorName("Mahi");
        return request;
    }

    private ThreadResponse createResponse() {
        ThreadResponse response = new ThreadResponse();
        response.setId(1L);
        response.setCourseId(1L);
        response.setLessonId(10L);
        response.setAuthorId(100L);
        response.setAuthorEmail("test@gmail.com");
        response.setAuthorName("Mahi");
        response.setAuthorRole("STUDENT");
        response.setTitle("Java Thread");
        response.setBody("Thread Body");
        response.setPinned(false);
        response.setClosed(false);
        response.setResolved(false);
        response.setReplyCount(0);
        return response;
    }

    // ----------------------------------------------------------------
    // CREATE THREAD TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Create Thread Success")
    void createThreadSuccess() throws Exception {

        ThreadRequest request = createRequest();
        ThreadResponse response = createResponse();

        when(discussionService.createThread(
                any(ThreadRequest.class),
                anyLong(),
                anyString(),
                anyString()
        )).thenReturn(response);

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Thread"))
                .andExpect(jsonPath("$.body").value("Thread Body"));
    }

    @Test
    @DisplayName("Create Thread Invalid CourseId")
    void createThreadInvalidCourseId() throws Exception {

        ThreadRequest request = createRequest();
        request.setCourseId(null);

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create Thread Blank Title")
    void createThreadBlankTitle() throws Exception {

        ThreadRequest request = createRequest();
        request.setTitle("");

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create Thread Blank Body")
    void createThreadBlankBody() throws Exception {

        ThreadRequest request = createRequest();
        request.setBody("");

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    // ----------------------------------------------------------------
    // GET THREADS TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Get Threads By Course")
    void getThreadsByCourse() throws Exception {

        ThreadResponse response = createResponse();

        when(discussionService.getThreadsByCourse(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/threads/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").value(1));
    }

    @Test
    @DisplayName("Get Threads By Course Empty")
    void getThreadsByCourseEmpty() throws Exception {

        when(discussionService.getThreadsByCourse(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/threads/course/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("Get Threads By Lesson")
    void getThreadsByLesson() throws Exception {

        ThreadResponse response = createResponse();

        when(discussionService.getThreadsByLesson(10L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/threads/lesson/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lessonId").value(10));
    }

    @Test
    @DisplayName("Get Thread By Id")
    void getThreadById() throws Exception {

        ThreadResponse response = createResponse();

        when(discussionService.getThreadById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/threads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Get Thread By Id Not Found")
    void getThreadByIdNotFound() throws Exception {

        when(discussionService.getThreadById(99L))
                .thenThrow(new ResourceNotFoundException("Thread not found"));

        mockMvc.perform(get("/api/threads/99"))
                .andExpect(status().isNotFound());
    }

    // ----------------------------------------------------------------
    // SEARCH TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Search Threads Success")
    void searchThreadsSuccess() throws Exception {

        ThreadResponse response = createResponse();

        when(discussionService.searchThreads(1L, "java"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/threads/course/1/search")
                        .param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Thread"));
    }

    @Test
    @DisplayName("Search Threads Empty")
    void searchThreadsEmpty() throws Exception {

        when(discussionService.searchThreads(1L, "xyz"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/threads/course/1/search")
                        .param("keyword", "xyz"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ----------------------------------------------------------------
    // DELETE TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Delete Thread Success")
    void deleteThreadSuccess() throws Exception {

        doNothing().when(discussionService)
                .deleteThread(anyLong(), anyLong(), anyString(), anyString());

        mockMvc.perform(delete("/api/threads/1")
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(content().string("Thread deleted successfully"));
    }

    @Test
    @DisplayName("Delete Thread Unauthorized")
    void deleteThreadUnauthorized() throws Exception {

        doThrow(new UnauthorizedException("Unauthorized"))
                .when(discussionService)
                .deleteThread(anyLong(), anyLong(), anyString(), anyString());

        mockMvc.perform(delete("/api/threads/1")
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------
    // PIN TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Pin Thread Success")
    void pinThreadSuccess() throws Exception {

        ThreadResponse response = createResponse();
        response.setPinned(true);

        when(discussionService.pinThread(anyLong(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/threads/1/pin")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    @DisplayName("Pin Thread Unauthorized")
    void pinThreadUnauthorized() throws Exception {

        when(discussionService.pinThread(anyLong(), anyString(), anyString()))
                .thenThrow(new UnauthorizedException("Unauthorized"));

        mockMvc.perform(put("/api/threads/1/pin")
                        .header("X-User-Email", "student@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------
    // UNPIN TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Unpin Thread Success")
    void unpinThreadSuccess() throws Exception {

        ThreadResponse response = createResponse();
        response.setPinned(false);

        when(discussionService.unpinThread(anyLong(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/threads/1/unpin")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(false));
    }

    // ----------------------------------------------------------------
    // CLOSE TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Close Thread Success")
    void closeThreadSuccess() throws Exception {

        ThreadResponse response = createResponse();
        response.setClosed(true);

        when(discussionService.closeThread(anyLong(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/threads/1/close")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closed").value(true));
    }

    // ----------------------------------------------------------------
    // REOPEN TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Reopen Thread Success")
    void reopenThreadSuccess() throws Exception {

        ThreadResponse response = createResponse();
        response.setClosed(false);

        when(discussionService.reopenThread(anyLong(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/threads/1/reopen")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closed").value(false));
    }

    // ----------------------------------------------------------------
    // HEADER VALIDATION TESTS
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Missing User Id Header")
    void missingUserIdHeader() throws Exception {

        ThreadRequest request = createRequest();

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Missing Email Header")
    void missingEmailHeader() throws Exception {

        ThreadRequest request = createRequest();

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Missing Role Header")
    void missingRoleHeader() throws Exception {

        ThreadRequest request = createRequest();

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com"))
                .andExpect(status().isInternalServerError());
    }

    // ----------------------------------------------------------------
    // INVALID PATH VARIABLE
    // ----------------------------------------------------------------

    @Test
    @DisplayName("Invalid Path Variable")
    void invalidPathVariable() throws Exception {

        mockMvc.perform(get("/api/threads/abc"))
                .andExpect(status().isInternalServerError());
    }
}