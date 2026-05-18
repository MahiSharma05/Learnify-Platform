package com.learnify.discussionservice.repository;

import com.learnify.discussionservice.entity.Reply;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReplyRepositoryTest {

    @Autowired
    private ReplyRepository replyRepository;

    // ---------------------------------------------------------
    // HELPER METHOD
    // ---------------------------------------------------------

    private Reply createReply(
            Long threadId,
            Long authorId,
            String body,
            boolean accepted,
            int upvotes,
            boolean deleted
    ) {

        Reply reply = new Reply();

        reply.setThreadId(threadId);
        reply.setAuthorId(authorId);
        reply.setAuthorEmail("test@gmail.com");
        reply.setAuthorName("Mahi");
        reply.setAuthorRole("STUDENT");
        reply.setBody(body);
        reply.setAccepted(accepted);
        reply.setUpvotes(upvotes);
        reply.setDeleted(deleted);

        return replyRepository.save(reply);
    }

    // ---------------------------------------------------------
    // SAVE TEST
    // ---------------------------------------------------------

    @Test
    @DisplayName("Save Reply")
    void saveReply() {

        Reply reply = createReply(
                1L,
                100L,
                "Reply Body",
                false,
                0,
                false
        );

        assertNotNull(reply.getId());
        assertEquals("Reply Body", reply.getBody());
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Id")
    void findById() {

        Reply saved = createReply(
                1L,
                100L,
                "Find Reply",
                false,
                0,
                false
        );

        Reply found = replyRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    // ---------------------------------------------------------
    // FIND ALL
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find All Replies")
    void findAllReplies() {

        createReply(1L, 100L,
                "Reply1", false, 0, false);

        createReply(1L, 101L,
                "Reply2", false, 0, false);

        List<Reply> list = replyRepository.findAll();

        assertEquals(2, list.size());
    }

    // ---------------------------------------------------------
    // DELETE REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete Reply")
    void deleteReply() {

        Reply reply = createReply(
                1L,
                100L,
                "Delete Reply",
                false,
                0,
                false
        );

        replyRepository.delete(reply);

        assertFalse(replyRepository.findById(reply.getId()).isPresent());
    }

    // ---------------------------------------------------------
    // UPDATE REPLY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Update Reply")
    void updateReply() {

        Reply reply = createReply(
                1L,
                100L,
                "Old Reply",
                false,
                0,
                false
        );

        reply.setBody("Updated Reply");

        Reply updated = replyRepository.save(reply);

        assertEquals("Updated Reply", updated.getBody());
    }

    // ---------------------------------------------------------
    // FIND BY THREAD ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Thread Id")
    void findByThreadId() {

        createReply(1L, 100L,
                "Reply1", false, 0, false);

        createReply(1L, 101L,
                "Reply2", false, 5, false);

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L);

        assertEquals(2, list.size());
    }

    // ---------------------------------------------------------
    // EMPTY THREAD REPLIES
    // ---------------------------------------------------------

    @Test
    @DisplayName("Empty Thread Replies")
    void emptyThreadReplies() {

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(99L);

        assertTrue(list.isEmpty());
    }

    // ---------------------------------------------------------
    // ACCEPTED REPLY FIRST
    // ---------------------------------------------------------

    @Test
    @DisplayName("Accepted Reply First")
    void acceptedReplyFirst() {

        createReply(1L, 100L,
                "Normal Reply", false, 10, false);

        createReply(1L, 101L,
                "Accepted Reply", true, 1, false);

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L);

        assertTrue(list.get(0).isAccepted());
    }

    // ---------------------------------------------------------
    // UPVOTES DESCENDING
    // ---------------------------------------------------------

    @Test
    @DisplayName("Upvotes Descending")
    void upvotesDescending() {

        createReply(1L, 100L,
                "Low", false, 1, false);

        createReply(1L, 101L,
                "High", false, 10, false);

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L);

        assertEquals(10, list.get(0).getUpvotes());
    }

    // ---------------------------------------------------------
    // FIND BY AUTHOR
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Author")
    void findByAuthor() {

        createReply(1L, 500L,
                "Author Reply", false, 0, false);

        List<Reply> list = replyRepository.findByAuthorId(500L);

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // COUNT ACTIVE REPLIES
    // ---------------------------------------------------------

    @Test
    @DisplayName("Count Active Replies")
    void countActiveReplies() {

        createReply(1L, 100L,
                "Reply1", false, 0, false);

        createReply(1L, 101L,
                "Reply2", false, 0, false);

        long count =
                replyRepository.countByThreadIdAndDeletedFalse(1L);

        assertEquals(2, count);
    }

    // ---------------------------------------------------------
    // DELETED REPLIES EXCLUDED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Deleted Replies Excluded")
    void deletedRepliesExcluded() {

        createReply(1L, 100L,
                "Deleted", false, 0, true);

        createReply(1L, 101L,
                "Active", false, 0, false);

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L);

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // FIND ALL THREAD REPLIES
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find All Thread Replies")
    void findAllThreadReplies() {

        createReply(1L, 100L,
                "Reply1", false, 0, false);

        createReply(1L, 101L,
                "Reply2", false, 0, true);

        List<Reply> list =
                replyRepository.findByThreadIdOrderByCreatedAtAsc(1L);

        assertEquals(2, list.size());
    }

    // ---------------------------------------------------------
    // EXISTS ACCEPTED TRUE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Exists Accepted True")
    void existsAcceptedTrue() {

        createReply(1L, 100L,
                "Accepted", true, 0, false);

        boolean exists =
                replyRepository.existsByThreadIdAndAcceptedTrue(1L);

        assertTrue(exists);
    }

    // ---------------------------------------------------------
    // EXISTS ACCEPTED FALSE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Exists Accepted False")
    void existsAcceptedFalse() {

        createReply(1L, 100L,
                "Not Accepted", false, 0, false);

        boolean exists =
                replyRepository.existsByThreadIdAndAcceptedTrue(1L);

        assertFalse(exists);
    }

    // ---------------------------------------------------------
    // DELETE BY THREAD ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete By Thread Id")
    void deleteByThreadId() {

        createReply(1L, 100L,
                "Reply1", false, 0, false);

        createReply(1L, 101L,
                "Reply2", false, 0, false);

        replyRepository.deleteByThreadId(1L);

        List<Reply> list =
                replyRepository.findByThreadIdOrderByCreatedAtAsc(1L);

        assertTrue(list.isEmpty());
    }

    // ---------------------------------------------------------
    // MULTIPLE REPLIES
    // ---------------------------------------------------------

    @Test
    @DisplayName("Multiple Replies")
    void multipleReplies() {

        createReply(1L, 100L,
                "Reply1", false, 0, false);

        createReply(1L, 101L,
                "Reply2", false, 0, false);

        createReply(1L, 102L,
                "Reply3", false, 0, false);

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L);

        assertEquals(3, list.size());
    }

    // ---------------------------------------------------------
    // SOFT DELETE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Soft Delete Reply")
    void softDeleteReply() {

        Reply reply = createReply(
                1L,
                100L,
                "Soft Delete",
                false,
                0,
                false
        );

        reply.setDeleted(true);

        Reply updated = replyRepository.save(reply);

        assertTrue(updated.isDeleted());
    }

    // ---------------------------------------------------------
    // ACCEPTED FLAG
    // ---------------------------------------------------------

    @Test
    @DisplayName("Accepted Flag Persistence")
    void acceptedFlagPersistence() {

        Reply reply = createReply(
                1L,
                100L,
                "Accepted",
                true,
                0,
                false
        );

        assertTrue(reply.isAccepted());
    }

    // ---------------------------------------------------------
    // UPVOTE PERSISTENCE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Upvote Persistence")
    void upvotePersistence() {

        Reply reply = createReply(
                1L,
                100L,
                "Popular Reply",
                false,
                15,
                false
        );

        assertEquals(15, reply.getUpvotes());
    }

    // ---------------------------------------------------------
    // AUTHOR ROLE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Author Role Persistence")
    void authorRolePersistence() {

        Reply reply = createReply(
                1L,
                100L,
                "Role Reply",
                false,
                0,
                false
        );

        assertEquals("STUDENT", reply.getAuthorRole());
    }

    // ---------------------------------------------------------
    // BODY PERSISTENCE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Body Persistence")
    void bodyPersistence() {

        Reply reply = createReply(
                1L,
                100L,
                "Discussion Reply",
                false,
                0,
                false
        );

        assertEquals("Discussion Reply", reply.getBody());
    }

    // ---------------------------------------------------------
    // THREAD ID PERSISTENCE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Thread Id Persistence")
    void threadIdPersistence() {

        Reply reply = createReply(
                999L,
                100L,
                "Thread Reply",
                false,
                0,
                false
        );

        assertEquals(999L, reply.getThreadId());
    }

    // ---------------------------------------------------------
    // REPLY ORDERING
    // ---------------------------------------------------------

    @Test
    @DisplayName("Reply Ordering")
    void replyOrdering() {

        createReply(1L, 100L,
                "Reply1", false, 2, false);

        createReply(1L, 101L,
                "Reply2", false, 8, false);

        List<Reply> list =
                replyRepository
                        .findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(1L);

        assertEquals(8, list.get(0).getUpvotes());
    }

    // ---------------------------------------------------------
    // NULL RESULT
    // ---------------------------------------------------------

    @Test
    @DisplayName("Null Result")
    void nullResult() {

        Reply reply = replyRepository.findById(999L).orElse(null);

        assertNull(reply);
    }
}