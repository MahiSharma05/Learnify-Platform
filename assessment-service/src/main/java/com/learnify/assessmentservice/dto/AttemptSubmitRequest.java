package com.learnify.assessmentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class AttemptSubmitRequest {

    /**
     * Map of questionId → studentAnswer
     *
     * Examples:
     *   MCQ:        { 1: "Option A" }
     *   TRUE_FALSE: { 2: "True" }
     *   MULTI:      { 3: "Option A,Option C" }
     *
     * If a question is skipped, it can be omitted (counts as wrong).
     */
    @NotNull(message = "Answers map is required")
    private Map<Long, String> answers;
}