package com.learnify.discussionservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ThreadResponse {

    private Long id;
    private Long courseId;
    private Long lessonId;
    private Long authorId;
    private String authorEmail;
    private String authorName;
    private String authorRole;
    private String title;
    private String body;
    private boolean pinned;
    private boolean closed;
    private boolean resolved;
    private int replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Replies included when fetching a single thread (not in list view)
    private List<ReplyResponse> replies;
}