package com.learnify.lessonservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ReorderRequest {

    // courseId whose lessons need to be reordered
    @NotNull(message = "courseId is required")
    private Long courseId;

    // Ordered list of lesson IDs in the desired new order
    // e.g. [3, 1, 2] means lesson 3 becomes order 1, lesson 1 becomes order 2, etc.
    @NotNull(message = "lessonIds list is required")
    private List<Long> lessonIds;
}