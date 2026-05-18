package com.learnify.discussionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnify.discussionservice.dto.ReplyRequest;
import com.learnify.discussionservice.dto.ReplyResponse;
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

@WebMvcTest(ReplyController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscussionService discussionService;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------------------

    private ReplyRequest createRequest() {
        ReplyRequest request = new ReplyRequest();
        request.setThreadId(1L);
        request.setBody("This is a reply");
        request.setAuthorName("Mahi");
        return request;
    }

    private ReplyResponse createResponse() {
        ReplyResponse response = new ReplyResponse();
        response.setId(1L);
        response.setThreadId(1L);
        response.setAuthorId(100L);
        response.setAuthorEmail("test@gmail.com");
        response.setAuthorName("Mahi");
        response.setAuthorRole("STUDENT");
        response.setBody("This is a reply");
        response.setAccepted(false);
        response.setUpvotes(0);
        response.setDeleted(false);
        return response;
    }

    // ------------------------------------------------------------
    // POST REPLY TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Post Reply Success")
    void postReplySuccess() throws Exception {

        ReplyRequest request = createRequest();
        ReplyResponse response = createResponse();

        when(discussionService.postReply(
                any(ReplyRequest.class),
                anyLong(),
                anyString(),
                anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.body").value("This is a reply"));
    }

    @Test
    @DisplayName("Post Reply Blank Body")
    void postReplyBlankBody() throws Exception {

        ReplyRequest request = createRequest();
        request.setBody("");

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post Reply Null ThreadId")
    void postReplyNullThreadId() throws Exception {

        ReplyRequest request = createRequest();
        request.setThreadId(null);

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // GET REPLIES TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Get Replies By Thread")
    void getRepliesByThread() throws Exception {

        ReplyResponse response = createResponse();

        when(discussionService.getRepliesByThread(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/replies/thread/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].threadId").value(1));
    }

    @Test
    @DisplayName("Get Replies By Thread Empty")
    void getRepliesByThreadEmpty() throws Exception {

        when(discussionService.getRepliesByThread(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/replies/thread/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("Get Replies Invalid Thread")
    void getRepliesInvalidThread() throws Exception {

        when(discussionService.getRepliesByThread(99L))
                .thenThrow(new ResourceNotFoundException("Thread not found"));

        mockMvc.perform(get("/api/replies/thread/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // DELETE REPLY TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Delete Reply Success")
    void deleteReplySuccess() throws Exception {

        doNothing().when(discussionService)
                .deleteReply(anyLong(), anyLong(), anyString(), anyString());

        mockMvc.perform(delete("/api/replies/1")
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reply deleted successfully"));
    }

    @Test
    @DisplayName("Delete Reply Unauthorized")
    void deleteReplyUnauthorized() throws Exception {

        doThrow(new UnauthorizedException("Unauthorized"))
                .when(discussionService)
                .deleteReply(anyLong(), anyLong(), anyString(), anyString());

        mockMvc.perform(delete("/api/replies/1")
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // UPVOTE TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Upvote Reply Success")
    void upvoteReplySuccess() throws Exception {

        ReplyResponse response = createResponse();
        response.setUpvotes(1);

        when(discussionService.upvoteReply(anyLong(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/replies/1/upvote")
                        .header("X-User-Email", "test@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(1));
    }

    @Test
    @DisplayName("Upvote Deleted Reply")
    void upvoteDeletedReply() throws Exception {

        when(discussionService.upvoteReply(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Reply deleted"));

        mockMvc.perform(put("/api/replies/1/upvote")
                        .header("X-User-Email", "test@gmail.com"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // ACCEPT REPLY TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Accept Reply Success")
    void acceptReplySuccess() throws Exception {

        ReplyResponse response = createResponse();
        response.setAccepted(true);

        when(discussionService.acceptReply(anyLong(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/replies/1/accept")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    @DisplayName("Accept Reply Unauthorized")
    void acceptReplyUnauthorized() throws Exception {

        when(discussionService.acceptReply(anyLong(), anyString(), anyString()))
                .thenThrow(new UnauthorizedException("Unauthorized"));

        mockMvc.perform(put("/api/replies/1/accept")
                        .header("X-User-Email", "student@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // UNACCEPT REPLY TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Unaccept Reply Success")
    void unacceptReplySuccess() throws Exception {

        ReplyResponse response = createResponse();
        response.setAccepted(false);

        when(discussionService.unacceptReply(anyLong(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/replies/1/unaccept")
                        .header("X-User-Email", "admin@gmail.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(false));
    }

    @Test
    @DisplayName("Unaccept Reply Unauthorized")
    void unacceptReplyUnauthorized() throws Exception {

        when(discussionService.unacceptReply(anyLong(), anyString(), anyString()))
                .thenThrow(new UnauthorizedException("Unauthorized"));

        mockMvc.perform(put("/api/replies/1/unaccept")
                        .header("X-User-Email", "student@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------
    // HEADER VALIDATION TESTS
    // ------------------------------------------------------------

    @Test
    @DisplayName("Missing User Id Header")
    void missingUserIdHeader() throws Exception {

        ReplyRequest request = createRequest();

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Email", "test@gmail.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Missing Email Header")
    void missingEmailHeader() throws Exception {

        ReplyRequest request = createRequest();

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Missing Role Header")
    void missingRoleHeader() throws Exception {

        ReplyRequest request = createRequest();

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 100)
                        .header("X-User-Email", "test@gmail.com"))
                .andExpect(status().isInternalServerError());
    }

    // ------------------------------------------------------------
    // INVALID PATH VARIABLE
    // ------------------------------------------------------------

    @Test
    @DisplayName("Invalid Reply Id")
    void invalidReplyId() throws Exception {

        mockMvc.perform(get("/api/replies/thread/abc"))
                .andExpect(status().isInternalServerError());
    }

    // ------------------------------------------------------------
    // MULTIPLE REPLIES TEST
    // ------------------------------------------------------------

    @Test
    @DisplayName("Multiple Replies Returned")
    void multipleRepliesReturned() throws Exception {

        ReplyResponse r1 = createResponse();

        ReplyResponse r2 = createResponse();
        r2.setId(2L);
        r2.setBody("Second reply");

        when(discussionService.getRepliesByThread(1L))
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/replies/thread/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ------------------------------------------------------------
    // ACCEPTED REPLY RETURNED
    // ------------------------------------------------------------

    @Test
    @DisplayName("Accepted Reply Returned")
    void acceptedReplyReturned() throws Exception {

        ReplyResponse response = createResponse();
        response.setAccepted(true);

        when(discussionService.getRepliesByThread(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/replies/thread/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accepted").value(true));
    }
}