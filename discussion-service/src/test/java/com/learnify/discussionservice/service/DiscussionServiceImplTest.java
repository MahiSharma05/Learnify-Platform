package com.learnify.discussionservice.service;

import com.learnify.discussionservice.dto.ReplyRequest;
import com.learnify.discussionservice.dto.ReplyResponse;
import com.learnify.discussionservice.dto.ThreadRequest;
import com.learnify.discussionservice.dto.ThreadResponse;
import com.learnify.discussionservice.entity.DiscussionThread;
import com.learnify.discussionservice.entity.Reply;
import com.learnify.discussionservice.exception.ResourceNotFoundException;
import com.learnify.discussionservice.exception.ThreadClosedException;
import com.learnify.discussionservice.exception.UnauthorizedException;
import com.learnify.discussionservice.repository.ReplyRepository;
import com.learnify.discussionservice.repository.ThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceImplTest {

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private ReplyRepository replyRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private DiscussionThread thread;
    private Reply reply;

    @BeforeEach
    void setup() {

        thread = new DiscussionThread();
        thread.setId(1L);
        thread.setCourseId(10L);
        thread.setLessonId(20L);
        thread.setAuthorId(100L);
        thread.setAuthorEmail("author@gmail.com");
        thread.setAuthorName("Mahi");
        thread.setAuthorRole("STUDENT");
        thread.setTitle("Java");
        thread.setBody("Java Body");
        thread.setPinned(false);
        thread.setClosed(false);
        thread.setResolved(false);
        thread.setReplyCount(0);

        reply = new Reply();
        reply.setId(1L);
        reply.setThreadId(1L);
        reply.setAuthorId(200L);
        reply.setAuthorEmail("reply@gmail.com");
        reply.setAuthorName("User");
        reply.setAuthorRole("STUDENT");
        reply.setBody("Reply Body");
        reply.setAccepted(false);
        reply.setUpvotes(0);
        reply.setDeleted(false);
    }

    // ---------------------------------------------------------
    // CREATE THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Create Thread Success")
    void createThreadSuccess() {

        ThreadRequest request = new ThreadRequest();
        request.setCourseId(10L);
        request.setLessonId(20L);
        request.setTitle("Java");
        request.setBody("Java Body");

        when(threadRepository.save(any(DiscussionThread.class)))
                .thenReturn(thread);

        ThreadResponse response = discussionService.createThread(
                request,
                100L,
                "author@gmail.com",
                "STUDENT"
        );

        assertNotNull(response);
        assertEquals("Java", response.getTitle());
    }

    // ---------------------------------------------------------
    // GET THREADS BY COURSE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Get Threads By Course")
    void getThreadsByCourse() {

        when(threadRepository
                .findByCourseIdOrderByPinnedDescCreatedAtDesc(10L))
                .thenReturn(List.of(thread));

        List<ThreadResponse> responses =
                discussionService.getThreadsByCourse(10L);

        assertEquals(1, responses.size());
    }

    // ---------------------------------------------------------
    // EMPTY COURSE THREADS
    // ---------------------------------------------------------

    @Test
    @DisplayName("Get Threads By Course Empty")
    void getThreadsByCourseEmpty() {

        when(threadRepository
                .findByCourseIdOrderByPinnedDescCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<ThreadResponse> responses =
                discussionService.getThreadsByCourse(10L);

        assertTrue(responses.isEmpty());
    }

    // ---------------------------------------------------------
    // GET THREADS BY LESSON
    // ---------------------------------------------------------

    @Test
    @DisplayName("Get Threads By Lesson")
    void getThreadsByLesson() {

        when(threadRepository
                .findByLessonIdOrderByPinnedDescCreatedAtDesc(20L))
                .thenReturn(List.of(thread));

        List<ThreadResponse> responses =
                discussionService.getThreadsByLesson(20L);

        assertEquals(1, responses.size());
    }

    // ---------------------------------------------------------
    // GET THREAD BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Get Thread By Id")
    void getThreadById() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository
                .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L))
                .thenReturn(List.of(reply));

        ThreadResponse response =
                discussionService.getThreadById(1L);

        assertNotNull(response);
        assertEquals(1, response.getReplies().size());
    }

    // ---------------------------------------------------------
    // THREAD NOT FOUND
    // ---------------------------------------------------------

    @Test
    @DisplayName("Thread Not Found")
    void threadNotFound() {

        when(threadRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> discussionService.getThreadById(99L));
    }

    // ---------------------------------------------------------
    // SEARCH THREADS
    // ---------------------------------------------------------

    @Test
    @DisplayName("Search Threads")
    void searchThreads() {

        when(threadRepository
                .findByCourseIdAndTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
                        10L,
                        "java",
                        "java"
                ))
                .thenReturn(List.of(thread));

        List<ThreadResponse> responses =
                discussionService.searchThreads(10L, "java");

        assertEquals(1, responses.size());
    }

    // ---------------------------------------------------------
    // DELETE THREAD SUCCESS
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete Thread Success")
    void deleteThreadSuccess() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        discussionService.deleteThread(
                1L,
                100L,
                "author@gmail.com",
                "STUDENT"
        );

        verify(replyRepository).deleteByThreadId(1L);
        verify(threadRepository).delete(thread);
    }

    // ---------------------------------------------------------
    // DELETE THREAD UNAUTHORIZED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete Thread Unauthorized")
    void deleteThreadUnauthorized() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        assertThrows(UnauthorizedException.class,
                () -> discussionService.deleteThread(
                        1L,
                        999L,
                        "x@gmail.com",
                        "STUDENT"
                ));
    }

    // ---------------------------------------------------------
    // PIN THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Pin Thread")
    void pinThread() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(threadRepository.save(any(DiscussionThread.class)))
                .thenReturn(thread);

        ThreadResponse response =
                discussionService.pinThread(
                        1L,
                        "admin@gmail.com",
                        "ADMIN"
                );

        assertNotNull(response);
    }

    // ---------------------------------------------------------
    // PIN THREAD UNAUTHORIZED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Pin Thread Unauthorized")
    void pinThreadUnauthorized() {

        assertThrows(UnauthorizedException.class,
                () -> discussionService.pinThread(
                        1L,
                        "student@gmail.com",
                        "STUDENT"
                ));
    }

    // ---------------------------------------------------------
    // CLOSE THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Close Thread")
    void closeThread() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(threadRepository.save(any(DiscussionThread.class)))
                .thenReturn(thread);

        ThreadResponse response =
                discussionService.closeThread(
                        1L,
                        "admin@gmail.com",
                        "ADMIN"
                );

        assertNotNull(response);
    }

    // ---------------------------------------------------------
    // REOPEN THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Reopen Thread")
    void reopenThread() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(threadRepository.save(any(DiscussionThread.class)))
                .thenReturn(thread);

        ThreadResponse response =
                discussionService.reopenThread(
                        1L,
                        "admin@gmail.com",
                        "ADMIN"
                );

        assertNotNull(response);
    }

    // ---------------------------------------------------------
    // POST REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Post Reply")
    void postReply() {

        ReplyRequest request = new ReplyRequest();
        request.setThreadId(1L);
        request.setBody("Reply");

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository.save(any(Reply.class)))
                .thenReturn(reply);

        when(threadRepository.save(any(DiscussionThread.class)))
                .thenReturn(thread);

        ReplyResponse response =
                discussionService.postReply(
                        request,
                        200L,
                        "reply@gmail.com",
                        "STUDENT"
                );

        assertNotNull(response);
        assertEquals("Reply Body", response.getBody());
    }

    // ---------------------------------------------------------
    // POST REPLY CLOSED THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Post Reply Closed Thread")
    void postReplyClosedThread() {

        thread.setClosed(true);

        ReplyRequest request = new ReplyRequest();
        request.setThreadId(1L);
        request.setBody("Reply");

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        assertThrows(ThreadClosedException.class,
                () -> discussionService.postReply(
                        request,
                        200L,
                        "reply@gmail.com",
                        "STUDENT"
                ));
    }

    // ---------------------------------------------------------
    // GET REPLIES
    // ---------------------------------------------------------

    @Test
    @DisplayName("Get Replies By Thread")
    void getRepliesByThread() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository
                .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L))
                .thenReturn(List.of(reply));

        List<ReplyResponse> responses =
                discussionService.getRepliesByThread(1L);

        assertEquals(1, responses.size());
    }

    // ---------------------------------------------------------
    // DELETE REPLY SUCCESS
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete Reply Success")
    void deleteReplySuccess() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository
                .countByThreadIdAndDeletedFalse(1L))
                .thenReturn(0L);

        discussionService.deleteReply(
                1L,
                200L,
                "reply@gmail.com",
                "STUDENT"
        );

        verify(replyRepository).save(any(Reply.class));
    }

    // ---------------------------------------------------------
    // DELETE REPLY UNAUTHORIZED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete Reply Unauthorized")
    void deleteReplyUnauthorized() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        assertThrows(UnauthorizedException.class,
                () -> discussionService.deleteReply(
                        1L,
                        999L,
                        "x@gmail.com",
                        "STUDENT"
                ));
    }

    // ---------------------------------------------------------
    // UPVOTE REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Upvote Reply")
    void upvoteReply() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(replyRepository.save(any(Reply.class)))
                .thenReturn(reply);

        ReplyResponse response =
                discussionService.upvoteReply(
                        1L,
                        "user@gmail.com"
                );

        assertNotNull(response);
    }

    // ---------------------------------------------------------
    // UPVOTE DELETED REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Upvote Deleted Reply")
    void upvoteDeletedReply() {

        reply.setDeleted(true);

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        assertThrows(ResourceNotFoundException.class,
                () -> discussionService.upvoteReply(
                        1L,
                        "user@gmail.com"
                ));
    }

    // ---------------------------------------------------------
    // ACCEPT REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Accept Reply")
    void acceptReply() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository.save(any(Reply.class)))
                .thenReturn(reply);

        ReplyResponse response =
                discussionService.acceptReply(
                        1L,
                        "author@gmail.com",
                        "STUDENT"
                );

        assertNotNull(response);
    }

    // ---------------------------------------------------------
    // ACCEPT REPLY UNAUTHORIZED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Accept Reply Unauthorized")
    void acceptReplyUnauthorized() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        assertThrows(UnauthorizedException.class,
                () -> discussionService.acceptReply(
                        1L,
                        "x@gmail.com",
                        "STUDENT"
                ));
    }

    // ---------------------------------------------------------
    // UNACCEPT REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Unaccept Reply")
    void unacceptReply() {

        reply.setAccepted(true);

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository.save(any(Reply.class)))
                .thenReturn(reply);

        when(replyRepository
                .existsByThreadIdAndAcceptedTrue(1L))
                .thenReturn(false);

        ReplyResponse response =
                discussionService.unacceptReply(
                        1L,
                        "author@gmail.com",
                        "STUDENT"
                );

        assertNotNull(response);
    }

    // ---------------------------------------------------------
    // UNACCEPT REPLY UNAUTHORIZED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Unaccept Reply Unauthorized")
    void unacceptReplyUnauthorized() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        assertThrows(UnauthorizedException.class,
                () -> discussionService.unacceptReply(
                        1L,
                        "x@gmail.com",
                        "STUDENT"
                ));
    }

    // ---------------------------------------------------------
    // REPLY NOT FOUND
    // ---------------------------------------------------------

    @Test
    @DisplayName("Reply Not Found")
    void replyNotFound() {

        when(replyRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> discussionService.upvoteReply(
                        99L,
                        "user@gmail.com"
                ));
    }

    // ---------------------------------------------------------
    // MODERATOR DELETE THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Moderator Delete Thread")
    void moderatorDeleteThread() {

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        discussionService.deleteThread(
                1L,
                999L,
                "admin@gmail.com",
                "ADMIN"
        );

        verify(threadRepository).delete(thread);
    }

    // ---------------------------------------------------------
    // MODERATOR DELETE REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Moderator Delete Reply")
    void moderatorDeleteReply() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository
                .countByThreadIdAndDeletedFalse(1L))
                .thenReturn(0L);

        discussionService.deleteReply(
                1L,
                999L,
                "admin@gmail.com",
                "ADMIN"
        );

        verify(replyRepository).save(any(Reply.class));
    }

    // ---------------------------------------------------------
    // THREAD RESOLVED TRUE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Thread Resolved True")
    void threadResolvedTrue() {

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository.save(any(Reply.class)))
                .thenReturn(reply);

        discussionService.acceptReply(
                1L,
                "author@gmail.com",
                "STUDENT"
        );

        assertTrue(thread.isResolved());
    }

    // ---------------------------------------------------------
    // THREAD RESOLVED FALSE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Thread Resolved False")
    void threadResolvedFalse() {

        reply.setAccepted(true);

        when(replyRepository.findById(1L))
                .thenReturn(Optional.of(reply));

        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        when(replyRepository.save(any(Reply.class)))
                .thenReturn(reply);

        when(replyRepository
                .existsByThreadIdAndAcceptedTrue(1L))
                .thenReturn(false);

        discussionService.unacceptReply(
                1L,
                "author@gmail.com",
                "STUDENT"
        );

        assertFalse(thread.isResolved());
    }
}