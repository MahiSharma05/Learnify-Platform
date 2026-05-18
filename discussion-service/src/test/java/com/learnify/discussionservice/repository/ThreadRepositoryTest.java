package com.learnify.discussionservice.repository;

import com.learnify.discussionservice.entity.DiscussionThread;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ThreadRepositoryTest {

    @Autowired
    private ThreadRepository threadRepository;

    // ---------------------------------------------------------
    // HELPER METHOD
    // ---------------------------------------------------------

    private DiscussionThread createThread(
            Long courseId,
            Long lessonId,
            Long authorId,
            String title,
            String body,
            boolean pinned,
            boolean resolved
    ) {

        DiscussionThread thread = new DiscussionThread();

        thread.setCourseId(courseId);
        thread.setLessonId(lessonId);
        thread.setAuthorId(authorId);
        thread.setAuthorEmail("test@gmail.com");
        thread.setAuthorName("Mahi");
        thread.setAuthorRole("STUDENT");
        thread.setTitle(title);
        thread.setBody(body);
        thread.setPinned(pinned);
        thread.setClosed(false);
        thread.setResolved(resolved);
        thread.setReplyCount(0);

        return threadRepository.save(thread);
    }

    // ---------------------------------------------------------
    // SAVE TEST
    // ---------------------------------------------------------

    @Test
    @DisplayName("Save Thread")
    void saveThread() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Java",
                "Java Body",
                false,
                false
        );

        assertNotNull(thread.getId());
        assertEquals("Java", thread.getTitle());
    }

    // ---------------------------------------------------------
    // FIND BY ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Id")
    void findById() {

        DiscussionThread saved = createThread(
                1L,
                10L,
                100L,
                "Spring",
                "Spring Body",
                false,
                false
        );

        DiscussionThread found =
                threadRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    // ---------------------------------------------------------
    // FIND ALL
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find All Threads")
    void findAllThreads() {

        createThread(1L, 10L, 100L,
                "Thread1", "Body1", false, false);

        createThread(1L, 11L, 101L,
                "Thread2", "Body2", false, false);

        List<DiscussionThread> list = threadRepository.findAll();

        assertEquals(2, list.size());
    }

    // ---------------------------------------------------------
    // DELETE THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Delete Thread")
    void deleteThread() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Delete",
                "Delete Body",
                false,
                false
        );

        threadRepository.delete(thread);

        assertFalse(threadRepository.findById(thread.getId()).isPresent());
    }

    // ---------------------------------------------------------
    // UPDATE THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Update Thread")
    void updateThread() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Old Title",
                "Body",
                false,
                false
        );

        thread.setTitle("New Title");

        DiscussionThread updated = threadRepository.save(thread);

        assertEquals("New Title", updated.getTitle());
    }

    // ---------------------------------------------------------
    // FIND BY COURSE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Course Id")
    void findByCourseId() {

        createThread(1L, 10L, 100L,
                "Thread1", "Body1", false, false);

        createThread(1L, 11L, 101L,
                "Thread2", "Body2", true, false);

        List<DiscussionThread> list =
                threadRepository.findByCourseIdOrderByPinnedDescCreatedAtDesc(1L);

        assertEquals(2, list.size());
    }

    // ---------------------------------------------------------
    // EMPTY COURSE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Empty Course Result")
    void emptyCourseResult() {

        List<DiscussionThread> list =
                threadRepository.findByCourseIdOrderByPinnedDescCreatedAtDesc(99L);

        assertTrue(list.isEmpty());
    }

    // ---------------------------------------------------------
    // FIND BY LESSON
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Lesson")
    void findByLesson() {

        createThread(1L, 50L, 100L,
                "Lesson Thread", "Body", false, false);

        List<DiscussionThread> list =
                threadRepository.findByLessonIdOrderByPinnedDescCreatedAtDesc(50L);

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // EMPTY LESSON
    // ---------------------------------------------------------

    @Test
    @DisplayName("Empty Lesson Result")
    void emptyLessonResult() {

        List<DiscussionThread> list =
                threadRepository.findByLessonIdOrderByPinnedDescCreatedAtDesc(500L);

        assertTrue(list.isEmpty());
    }

    // ---------------------------------------------------------
    // FIND BY AUTHOR
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find By Author")
    void findByAuthor() {

        createThread(1L, 10L, 200L,
                "Author Thread", "Body", false, false);

        List<DiscussionThread> list =
                threadRepository.findByAuthorIdOrderByCreatedAtDesc(200L);

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // PINNED THREADS
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find Pinned Threads")
    void findPinnedThreads() {

        createThread(1L, 10L, 100L,
                "Pinned", "Body", true, false);

        createThread(1L, 11L, 101L,
                "NotPinned", "Body", false, false);

        List<DiscussionThread> list =
                threadRepository.findByCourseIdAndPinnedTrue(1L);

        assertEquals(1, list.size());
        assertTrue(list.get(0).isPinned());
    }

    // ---------------------------------------------------------
    // MULTIPLE PINNED THREADS
    // ---------------------------------------------------------

    @Test
    @DisplayName("Multiple Pinned Threads")
    void multiplePinnedThreads() {

        createThread(1L, 10L, 100L,
                "Pinned1", "Body", true, false);

        createThread(1L, 11L, 101L,
                "Pinned2", "Body", true, false);

        List<DiscussionThread> list =
                threadRepository.findByCourseIdAndPinnedTrue(1L);

        assertEquals(2, list.size());
    }

    // ---------------------------------------------------------
    // SEARCH TITLE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Search By Title")
    void searchByTitle() {

        createThread(1L, 10L, 100L,
                "Java Spring", "Body", false, false);

        List<DiscussionThread> list =
                threadRepository
                        .findByCourseIdAndTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
                                1L,
                                "Java",
                                "Java"
                        );

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // SEARCH BODY
    // ---------------------------------------------------------

    @Test
    @DisplayName("Search By Body")
    void searchByBody() {

        createThread(1L, 10L, 100L,
                "Title",
                "Spring Boot Discussion",
                false,
                false);

        List<DiscussionThread> list =
                threadRepository
                        .findByCourseIdAndTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
                                1L,
                                "Spring",
                                "Spring"
                        );

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // CASE INSENSITIVE SEARCH
    // ---------------------------------------------------------

    @Test
    @DisplayName("Case Insensitive Search")
    void caseInsensitiveSearch() {

        createThread(1L, 10L, 100L,
                "JAVA",
                "BODY",
                false,
                false);

        List<DiscussionThread> list =
                threadRepository
                        .findByCourseIdAndTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(
                                1L,
                                "java",
                                "java"
                        );

        assertEquals(1, list.size());
    }

    // ---------------------------------------------------------
    // COUNT BY COURSE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Count By Course")
    void countByCourse() {

        createThread(1L, 10L, 100L,
                "Thread1", "Body", false, false);

        createThread(1L, 11L, 101L,
                "Thread2", "Body", false, false);

        long count = threadRepository.countByCourseId(1L);

        assertEquals(2, count);
    }

    // ---------------------------------------------------------
    // FIND UNRESOLVED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Find Unresolved Threads")
    void findUnresolvedThreads() {

        createThread(1L, 10L, 100L,
                "Resolved", "Body", false, true);

        createThread(1L, 11L, 101L,
                "Unresolved", "Body", false, false);

        List<DiscussionThread> list =
                threadRepository
                        .findByCourseIdAndResolvedFalseOrderByCreatedAtDesc(1L);

        assertEquals(1, list.size());
        assertFalse(list.get(0).isResolved());
    }

    // ---------------------------------------------------------
    // RESOLVED EXCLUDED
    // ---------------------------------------------------------

    @Test
    @DisplayName("Resolved Thread Excluded")
    void resolvedThreadExcluded() {

        createThread(1L, 10L, 100L,
                "Resolved", "Body", false, true);

        List<DiscussionThread> list =
                threadRepository
                        .findByCourseIdAndResolvedFalseOrderByCreatedAtDesc(1L);

        assertTrue(list.isEmpty());
    }

    // ---------------------------------------------------------
    // NULL LESSON ID
    // ---------------------------------------------------------

    @Test
    @DisplayName("Null Lesson Id")
    void nullLessonId() {

        DiscussionThread thread = createThread(
                1L,
                null,
                100L,
                "No Lesson",
                "Body",
                false,
                false
        );

        assertNull(thread.getLessonId());
    }

    // ---------------------------------------------------------
    // REPLY COUNT PERSISTENCE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Reply Count Persistence")
    void replyCountPersistence() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Reply Count",
                "Body",
                false,
                false
        );

        thread.setReplyCount(5);

        DiscussionThread updated = threadRepository.save(thread);

        assertEquals(5, updated.getReplyCount());
    }

    // ---------------------------------------------------------
    // CLOSED THREAD
    // ---------------------------------------------------------

    @Test
    @DisplayName("Closed Thread")
    void closedThread() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Closed",
                "Body",
                false,
                false
        );

        thread.setClosed(true);

        DiscussionThread updated = threadRepository.save(thread);

        assertTrue(updated.isClosed());
    }

    // ---------------------------------------------------------
    // PINNED THREAD TRUE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Pinned Thread True")
    void pinnedThreadTrue() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Pinned",
                "Body",
                true,
                false
        );

        assertTrue(thread.isPinned());
    }

    // ---------------------------------------------------------
    // AUTHOR ROLE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Author Role Persistence")
    void authorRolePersistence() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Role",
                "Body",
                false,
                false
        );

        assertEquals("STUDENT", thread.getAuthorRole());
    }

    // ---------------------------------------------------------
    // BODY PERSISTENCE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Body Persistence")
    void bodyPersistence() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Title",
                "Discussion Body",
                false,
                false
        );

        assertEquals("Discussion Body", thread.getBody());
    }

    // ---------------------------------------------------------
    // TITLE PERSISTENCE
    // ---------------------------------------------------------

    @Test
    @DisplayName("Title Persistence")
    void titlePersistence() {

        DiscussionThread thread = createThread(
                1L,
                10L,
                100L,
                "Important Thread",
                "Body",
                false,
                false
        );

        assertEquals("Important Thread", thread.getTitle());
    }
}