package com.learnify.lessonservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResourceResponse {

    private Long id;
    private Long lessonId;
    private String name;
    private String fileUrl;
    private String fileType;
    private Long sizeKb;
    private LocalDateTime uploadedAt;
}