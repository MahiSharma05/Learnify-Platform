package com.learnify.discussionservice.repository;

import com.learnify.discussionservice.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // All non-deleted replies for a thread — accepted first, then by upvotes
    List<Reply> findByThreadIdAndDeletedFalseOrderByAcceptedDescUpvotesDescCreatedAtAsc(
            Long threadId);

    // All replies by an author (including deleted — for moderation)
    List<Reply> findByAuthorId(Long authorId);

    // Count non-deleted replies for a thread (for replyCount sync)
    long countByThreadIdAndDeletedFalse(Long threadId);

    // All replies for a thread including deleted (admin moderation view)
    List<Reply> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    // Check if a reply has already been accepted in a thread
    boolean existsByThreadIdAndAcceptedTrue(Long threadId);

    // Delete all replies when a thread is deleted
    void deleteByThreadId(Long threadId);
}