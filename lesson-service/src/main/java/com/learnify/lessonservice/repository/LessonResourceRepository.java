package com.learnify.lessonservice.repository;

import com.learnify.lessonservice.entity.LessonResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {

    // All resources attached to a specific lesson
    List<LessonResource> findByLessonId(Long lessonId);

    // Delete all resources for a lesson (called when lesson is deleted)
    void deleteByLessonId(Long lessonId);
}