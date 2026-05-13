package com.learnify.courseservice.service;

import com.learnify.courseservice.dto.CourseRequest;
import com.learnify.courseservice.entity.Course;
import com.learnify.courseservice.enums.ApprovalStatus;
import com.learnify.courseservice.repository.CourseRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Create Course (Instructor)
    @Override
    @Caching(evict = {
            @CacheEvict(value = "courses", key = "'all'"),
            @CacheEvict(value = "courses", key = "'featured'")
    })
    public Course createCourse(CourseRequest request, String email, String role) {

        System.out.println("Service EMAIL: " + email);
        System.out.println("Service ROLE: " + role);

        if (role == null || !role.trim().equalsIgnoreCase("INSTRUCTOR")) {
            throw new RuntimeException("Only instructors can create courses");
        }

        Course course = new Course();

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setLevel(request.getLevel());
        course.setPrice(request.getPrice());
        course.setLanguage(request.getLanguage());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setTotalDurationMinutes(request.getTotalDurationMinutes());

        course.setInstructorId(request.getInstructorId());
        course.setInstructorName(email);

        course.setApprovalStatus(ApprovalStatus.DRAFT);
        course.setPublished(false);
        course.setFeatured(false);

        return courseRepository.save(course);
    }

    // Get course by ID
    @Override
    @Cacheable(value = "courses", key = "#id")
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    // Public catalog (only published courses)
    @Override
    @Cacheable(value = "courses", key = "'all'")
    public List<Course> getAllPublishedCourses() {
        return courseRepository.findByPublishedTrue();
    }

    // Instructor's own courses
    @Override
    public List<Course> getCoursesByInstructor(String email) {
        return courseRepository.findByInstructorName(email);
    }

    // Search courses by keyword
    @Override
    @Cacheable(value = "courseSearch", key = "#keyword")
    public List<Course> searchCourses(String keyword) {
        return courseRepository.searchByKeyword(keyword);
    }

    // Filter by category
    @Override
    @Cacheable(value = "courses", key = "'cat_' + #category")
    public List<Course> filterByCategory(String category) {
        return courseRepository.findByCategoryAndPublishedTrue(category);
    }

    // Get instructor courses by ID
    @Override
    public List<Course> getCoursesByInstructorId(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    // Filter by level
    @Override
    public List<Course> filterByLevel(String level) {
        return courseRepository.findByLevelAndPublishedTrue(level);
    }

    // Filter by price
    @Override
    public List<Course> filterByPrice(Double price) {
        return courseRepository.findByPriceLessThanEqualAndPublishedTrue(price);
    }

    // Update course (only owner can update)
    @Override
    @Caching(evict = {
            @CacheEvict(value = "courses", key = "#id"),
            @CacheEvict(value = "courses", key = "'all'"),
            @CacheEvict(value = "courses", key = "'featured'"),
            @CacheEvict(value = "courseSearch", allEntries = true)
    })
    public Course updateCourse(Long id, CourseRequest request, String email, String role) {

        Course course = getCourseById(id);

        if (!course.getInstructorName().equals(email) && !role.equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("You are not allowed to update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setLevel(request.getLevel());
        course.setPrice(request.getPrice());
        course.setLanguage(request.getLanguage());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setTotalDurationMinutes(request.getTotalDurationMinutes());

        return courseRepository.save(course);
    }

    // Submit course for admin review
    @Override
    @Caching(evict = {
            @CacheEvict(value = "courses", key = "#id"),
            @CacheEvict(value = "courses", key = "'all'"),
            @CacheEvict(value = "courses", key = "'featured'")
    })
    public void publishCourse(Long id) {

        Course course = getCourseById(id);

        course.setApprovalStatus(ApprovalStatus.PENDING);
        course.setPublished(false);

        courseRepository.save(course);
    }

    // Admin approves course
    @Override
    @Caching(evict = {
            @CacheEvict(value = "courses", key = "#id"),
            @CacheEvict(value = "courses", key = "'all'"),
            @CacheEvict(value = "courses", key = "'featured'")
    })
    public void approveCourse(Long id) {

        Course course = getCourseById(id);

        course.setApprovalStatus(ApprovalStatus.APPROVED);
        course.setPublished(true);

        courseRepository.save(course);
    }

    // Admin rejects course
    @Override
    @CacheEvict(value = "courses", key = "#id")
    public void rejectCourse(Long id) {

        Course course = getCourseById(id);

        course.setApprovalStatus(ApprovalStatus.REJECTED);
        course.setPublished(false);

        courseRepository.save(course);
    }

    // Delete course
    @Override
    @Caching(evict = {
            @CacheEvict(value = "courses", key = "#id"),
            @CacheEvict(value = "courses", key = "'all'"),
            @CacheEvict(value = "courses", key = "'featured'"),
            @CacheEvict(value = "courseSearch", allEntries = true)
    })
    public void deleteCourse(Long id, String email, String role) {

        Course course = getCourseById(id);

        if (!course.getInstructorName().equals(email) && !role.equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("You are not allowed to delete this course");
        }

        courseRepository.delete(course);
    }

    // Get pending courses
    @Override
    public List<Course> getPendingCourses() {
        return courseRepository.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    // Featured courses
    @Override
    @Cacheable(value = "courses", key = "'featured'")
    public List<Course> getFeaturedCourses() {
        return courseRepository.findByFeaturedTrueAndPublishedTrue();
    }

    @Override
    public List<Course> getAllCoursesForAdmin() {
        return courseRepository.findAll();
    }
}