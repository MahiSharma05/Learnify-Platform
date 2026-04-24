package com.learnify.discussionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyRequest {

    @NotNull(message = "threadId is required")
    private Long threadId;

    @NotBlank(message = "Reply body is required")
    private String body;

    // Populated from Gateway headers in controller
    private String authorName;
}