package com.learnify.lessonservice.service;

import com.learnify.lessonservice.dto.*;

import java.util.List;

public interface LessonService {

    // ── Lesson CRUD ──────────────────────────────────────────────────────────

    // Add a new lesson to a course (INSTRUCTOR only)
    LessonResponse addLesson(LessonRequest request, String userEmail, String userRole);

    // Get all lessons of a course ordered by orderIndex
    List<LessonResponse> getLessonsByCourse(Long courseId);

    // Get a single lesson by ID
    LessonResponse getLessonById(Long id);

    // Update lesson details (INSTRUCTOR = owner, or ADMIN)
    LessonResponse updateLesson(Long id, LessonRequest request, String userEmail, String userRole);

    // Delete a lesson (and its resources) (INSTRUCTOR = owner, or ADMIN)
    void deleteLesson(Long id, String userEmail, String userRole);

    // Reorder lessons within a course
    void reorderLessons(ReorderRequest request, String userEmail, String userRole);

    // Get only preview-enabled lessons for a course (public access)
    List<LessonResponse> getPreviewLessons(Long courseId);

    // ── Resource Management ──────────────────────────────────────────────────

    // Attach a supplementary resource to a lesson
    ResourceResponse addResource(Long lessonId, ResourceRequest request,
                                 String userEmail, String userRole);

    // Remove a resource from a lesson
    void removeResource(Long lessonId, Long resourceId, String userEmail, String userRole);

    // Get all resources for a lesson
    List<ResourceResponse> getResourcesByLesson(Long lessonId);
}