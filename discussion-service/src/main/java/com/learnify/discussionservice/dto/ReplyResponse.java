package com.learnify.discussionservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReplyResponse {

    private Long id;
    private Long threadId;
    private Long authorId;
    private String authorEmail;
    private String authorName;
    private String authorRole;
    private String body;
    private boolean accepted;   // Best answer marker
    private int upvotes;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}