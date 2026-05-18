package com.learnify.lessonservice.service;

import com.learnify.lessonservice.dto.*;
import com.learnify.lessonservice.entity.Lesson;
import com.learnify.lessonservice.entity.LessonResource;
import com.learnify.lessonservice.exception.ResourceNotFoundException;
import com.learnify.lessonservice.exception.UnauthorizedException;
import com.learnify.lessonservice.repository.LessonRepository;
import com.learnify.lessonservice.repository.LessonResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonResourceRepository lessonResourceRepository;

    // Constructor injection (consistent with course-service pattern)
    public LessonServiceImpl(LessonRepository lessonRepository,
                             LessonResourceRepository lessonResourceRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonResourceRepository = lessonResourceRepository;
    }

    // ── Lesson CRUD ──────────────────────────────────────────────────────────

    @Override
    public LessonResponse addLesson(LessonRequest request, String userEmail, String userRole) {

        // Only INSTRUCTOR or ADMIN can add lessons
        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can add lessons");
        }

        Lesson lesson = new Lesson();
        lesson.setCourseId(request.getCourseId());
        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType().toUpperCase());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setDurationMinutes(request.getDurationMinutes());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setDescription(request.getDescription());
        lesson.setPreview(request.isPreview());

        Lesson saved = lessonRepository.save(lesson);
        return mapToResponse(saved, new ArrayList<>());
    }

    @Override
    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndex(courseId);

        return lessons.stream()
                .map(lesson -> {
                    List<LessonResource> resources = lessonResourceRepository.findByLessonId(lesson.getId());
                    return mapToResponse(lesson, resources);
                })
                .collect(Collectors.toList());
    }

    @Override
    public LessonResponse getLessonById(Long id) {
        Lesson lesson = findLessonOrThrow(id);
        List<LessonResource> resources = lessonResourceRepository.findByLessonId(id);
        return mapToResponse(lesson, resources);
    }

    @Override
    public LessonResponse updateLesson(Long id, LessonRequest request,
                                       String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can update lessons");
        }

        Lesson lesson = findLessonOrThrow(id);

        lesson.setTitle(request.getTitle());
        lesson.setContentType(request.getContentType().toUpperCase());
        lesson.setContentUrl(request.getContentUrl());
        lesson.setDurationMinutes(request.getDurationMinutes());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setDescription(request.getDescription());
        lesson.setPreview(request.isPreview());

        Lesson updated = lessonRepository.save(lesson);
        List<LessonResource> resources = lessonResourceRepository.findByLessonId(id);
        return mapToResponse(updated, resources);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id, String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can delete lessons");
        }

        Lesson lesson = findLessonOrThrow(id);

        // Delete all attached resources first (avoid orphan records)
        lessonResourceRepository.deleteByLessonId(id);

        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional
    public void reorderLessons(ReorderRequest request, String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can reorder lessons");
        }

        List<Long> lessonIds = request.getLessonIds();

        // Assign new orderIndex based on position in the given list
        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

            // Validate the lesson belongs to the given course
            if (!lesson.getCourseId().equals(request.getCourseId())) {
                throw new UnauthorizedException("Lesson " + lessonId +
                        " does not belong to course " + request.getCourseId());
            }

            lesson.setOrderIndex(i + 1); // 1-based
            lessonRepository.save(lesson);
        }
    }

    @Override
    public List<LessonResponse> getPreviewLessons(Long courseId) {
        List<Lesson> previews = lessonRepository
                .findByCourseIdAndIsPreviewTrueOrderByOrderIndex(courseId);

        return previews.stream()
                .map(lesson -> {
                    List<LessonResource> resources = lessonResourceRepository.findByLessonId(lesson.getId());
                    return mapToResponse(lesson, resources);
                })
                .collect(Collectors.toList());
    }

    // ── Resource Management ──────────────────────────────────────────────────

    @Override
    public ResourceResponse addResource(Long lessonId, ResourceRequest request,
                                        String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can add resources");
        }

        // Ensure lesson exists
        findLessonOrThrow(lessonId);

        LessonResource resource = new LessonResource();
        resource.setLessonId(lessonId);
        resource.setName(request.getName());
        resource.setFileUrl(request.getFileUrl());
        resource.setFileType(request.getFileType() != null
                ? request.getFileType().toUpperCase() : "OTHER");
        resource.setSizeKb(request.getSizeKb());

        LessonResource saved = lessonResourceRepository.save(resource);
        return mapResourceToResponse(saved);
    }

    @Override
    @Transactional
    public void removeResource(Long lessonId, Long resourceId,
                               String userEmail, String userRole) {

        if (!isInstructorOrAdmin(userRole)) {
            throw new UnauthorizedException("Only instructors or admins can remove resources");
        }

        // Ensure lesson exists
        findLessonOrThrow(lessonId);

        LessonResource resource = lessonResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + resourceId));

        // Ensure resource belongs to the specified lesson
        if (!resource.getLessonId().equals(lessonId)) {
            throw new UnauthorizedException("Resource does not belong to lesson " + lessonId);
        }

        lessonResourceRepository.delete(resource);
    }

    @Override
    public List<ResourceResponse> getResourcesByLesson(Long lessonId) {
        // Ensure lesson exists
        findLessonOrThrow(lessonId);

        return lessonResourceRepository.findByLessonId(lessonId)
                .stream()
                .map(this::mapResourceToResponse)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Lesson findLessonOrThrow(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
    }

    private boolean isInstructorOrAdmin(String role) {
        return "INSTRUCTOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    // Map Lesson entity → LessonResponse DTO
    private LessonResponse mapToResponse(Lesson lesson, List<LessonResource> resources) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setCourseId(lesson.getCourseId());
        response.setTitle(lesson.getTitle());
        response.setContentType(lesson.getContentType());
        response.setContentUrl(lesson.getContentUrl());
        response.setDurationMinutes(lesson.getDurationMinutes());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setDescription(lesson.getDescription());
        response.setPreview(lesson.isPreview());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        response.setResources(resources.stream()
                .map(this::mapResourceToResponse)
                .collect(Collectors.toList()));
        return response;
    }

    // Map LessonResource entity → ResourceResponse DTO
    private ResourceResponse mapResourceToResponse(LessonResource resource) {
        ResourceResponse response = new ResourceResponse();
        response.setId(resource.getId());
        response.setLessonId(resource.getLessonId());
        response.setName(resource.getName());
        response.setFileUrl(resource.getFileUrl());
        response.setFileType(resource.getFileType());
        response.setSizeKb(resource.getSizeKb());
        response.setUploadedAt(resource.getUploadedAt());
        return response;
    }
}