package com.learnify.discussionservice.repository;

import com.learnify.discussionservice.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThreadRepository extends JpaRepository<DiscussionThread, Long> {

    // All threads for a course — pinned first, then newest
    List<DiscussionThread> findByCourseIdOrderByPinnedDescCreatedAtDesc(Long courseId);

    // All threads for a specific lesson
    List<DiscussionThread> findByLessonIdOrderByPinnedDescCreatedAtDesc(Long lessonId);

    // All threads created by an author
    List<DiscussionThread> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Only pinned threads for a course
    List<DiscussionThread> findByCourseIdAndPinnedTrue(Long courseId);

    // Search threads by keyword in title or body
    List<DiscussionThread> findByCourseIdAndTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
            Long courseId, String titleKeyword, String bodyKeyword);

    // Count threads for a course (analytics)
    long countByCourseId(Long courseId);

    // All unresolved threads for a course (for instructor attention)
    List<DiscussionThread> findByCourseIdAndResolvedFalseOrderByCreatedAtDesc(Long courseId);
}