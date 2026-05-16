package com.learnify.lessonservice.repository;

import com.learnify.lessonservice.entity.LessonResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LessonResourceRepositoryTest {

    @Autowired
    private LessonResourceRepository repository;

    private LessonResource createResource(Long lessonId,
                                          String name,
                                          String fileUrl,
                                          String fileType,
                                          Long sizeKb) {

        LessonResource resource = new LessonResource();
        resource.setLessonId(lessonId);
        resource.setName(name);
        resource.setFileUrl(fileUrl);
        resource.setFileType(fileType);
        resource.setSizeKb(sizeKb);

        return repository.save(resource);
    }

    @Test
    @DisplayName("Should save resource successfully")
    void shouldSaveResourceSuccessfully() {

        LessonResource resource =
                createResource(1L, "PDF", "url", "PDF", 100L);

        assertNotNull(resource.getId());
    }

    @Test
    @DisplayName("Should find resources by lesson id")
    void shouldFindResourcesByLessonId() {

        createResource(1L, "PDF", "url", "PDF", 100L);

        List<LessonResource> resources =
                repository.findByLessonId(1L);

        assertEquals(1, resources.size());
    }

    @Test
    @DisplayName("Should return empty list when lesson has no resources")
    void shouldReturnEmptyListWhenLessonHasNoResources() {

        List<LessonResource> resources =
                repository.findByLessonId(99L);

        assertTrue(resources.isEmpty());
    }

    @Test
    @DisplayName("Should delete resources by lesson id")
    void shouldDeleteResourcesByLessonId() {

        createResource(1L, "PDF", "url", "PDF", 100L);

        repository.deleteByLessonId(1L);

        assertEquals(0,
                repository.findByLessonId(1L).size());
    }

    @Test
    @DisplayName("Should save multiple resources")
    void shouldSaveMultipleResources() {

        createResource(1L, "PDF1", "url1", "PDF", 100L);
        createResource(1L, "PDF2", "url2", "PDF", 200L);

        List<LessonResource> resources =
                repository.findByLessonId(1L);

        assertEquals(2, resources.size());
    }

    @Test
    @DisplayName("Should find resource by id")
    void shouldFindResourceById() {

        LessonResource resource =
                createResource(1L, "PDF", "url", "PDF", 100L);

        Optional<LessonResource> found =
                repository.findById(resource.getId());

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should return empty when resource id not found")
    void shouldReturnEmptyWhenResourceIdNotFound() {

        Optional<LessonResource> found =
                repository.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should update resource successfully")
    void shouldUpdateResourceSuccessfully() {

        LessonResource resource =
                createResource(1L, "PDF", "url", "PDF", 100L);

        resource.setName("UPDATED");

        repository.save(resource);

        LessonResource updated =
                repository.findById(resource.getId()).orElse(null);

        assertNotNull(updated);
        assertEquals("UPDATED", updated.getName());
    }

    @Test
    @DisplayName("Should delete single resource")
    void shouldDeleteSingleResource() {

        LessonResource resource =
                createResource(1L, "PDF", "url", "PDF", 100L);

        repository.delete(resource);

        Optional<LessonResource> deleted =
                repository.findById(resource.getId());

        assertTrue(deleted.isEmpty());
    }

    @Test
    @DisplayName("Should store file type correctly")
    void shouldStoreFileTypeCorrectly() {

        LessonResource resource =
                createResource(1L, "Slides", "url", "SLIDES", 200L);

        assertEquals("SLIDES", resource.getFileType());
    }

    @Test
    @DisplayName("Should store size correctly")
    void shouldStoreSizeCorrectly() {

        LessonResource resource =
                createResource(1L, "Zip", "url", "ZIP", 500L);

        assertEquals(500L, resource.getSizeKb());
    }

    @Test
    @DisplayName("Should store lesson id correctly")
    void shouldStoreLessonIdCorrectly() {

        LessonResource resource =
                createResource(5L, "PDF", "url", "PDF", 100L);

        assertEquals(5L, resource.getLessonId());
    }

    @Test
    @DisplayName("Should persist uploaded time")
    void shouldPersistUploadedTime() {

        LessonResource resource =
                createResource(1L, "PDF", "url", "PDF", 100L);

        assertNotNull(resource.getUploadedAt());
    }

    @Test
    @DisplayName("Should handle different file types")
    void shouldHandleDifferentFileTypes() {

        createResource(1L, "PDF", "url1", "PDF", 100L);
        createResource(1L, "CODE", "url2", "CODE", 200L);
        createResource(1L, "ZIP", "url3", "ZIP", 300L);

        List<LessonResource> resources =
                repository.findByLessonId(1L);

        assertEquals(3, resources.size());
    }

    @Test
    @DisplayName("Should delete only targeted lesson resources")
    void shouldDeleteOnlyTargetedLessonResources() {

        createResource(1L, "PDF1", "url1", "PDF", 100L);
        createResource(2L, "PDF2", "url2", "PDF", 100L);

        repository.deleteByLessonId(1L);

        List<LessonResource> lesson1 =
                repository.findByLessonId(1L);

        List<LessonResource> lesson2 =
                repository.findByLessonId(2L);

        assertEquals(0, lesson1.size());
        assertEquals(1, lesson2.size());
    }

    @Test
    @DisplayName("Should count resources correctly")
    void shouldCountResourcesCorrectly() {

        createResource(1L, "A", "url1", "PDF", 100L);
        createResource(1L, "B", "url2", "PDF", 200L);
        createResource(1L, "C", "url3", "PDF", 300L);

        List<LessonResource> resources =
                repository.findByLessonId(1L);

        assertEquals(3, resources.size());
    }

    @Test
    @DisplayName("Should allow duplicate resource names")
    void shouldAllowDuplicateResourceNames() {

        createResource(1L, "PDF", "url1", "PDF", 100L);
        createResource(1L, "PDF", "url2", "PDF", 200L);

        List<LessonResource> resources =
                repository.findByLessonId(1L);

        assertEquals(2, resources.size());
    }

    @Test
    @DisplayName("Should allow different lessons with resources")
    void shouldAllowDifferentLessonsWithResources() {

        createResource(1L, "A", "url1", "PDF", 100L);
        createResource(2L, "B", "url2", "PDF", 200L);

        assertEquals(1,
                repository.findByLessonId(1L).size());

        assertEquals(1,
                repository.findByLessonId(2L).size());
    }

    @Test
    @DisplayName("Should persist resource name")
    void shouldPersistResourceName() {

        LessonResource resource =
                createResource(1L, "NOTES", "url", "PDF", 100L);

        assertEquals("NOTES", resource.getName());
    }

    @Test
    @DisplayName("Should persist file url")
    void shouldPersistFileUrl() {

        LessonResource resource =
                createResource(1L, "PDF", "my-url", "PDF", 100L);

        assertEquals("my-url", resource.getFileUrl());
    }
}