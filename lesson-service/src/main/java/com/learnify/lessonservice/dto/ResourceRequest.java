package com.learnify.lessonservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    private String name;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    // PDF, SLIDES, CODE, ZIP, OTHER
    private String fileType;

    private Long sizeKb;
}