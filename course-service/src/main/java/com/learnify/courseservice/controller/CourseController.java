package com.learnify.courseservice.controller;

import com.learnify.courseservice.dto.CourseRequest;
import com.learnify.courseservice.entity.Course;
import com.learnify.courseservice.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // 🔹 Create Course (INSTRUCTOR only)
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        // DEBUG (VERY IMPORTANT)
        System.out.println("Controller EMAIL: " + email);
        System.out.println("Controller ROLE: " + role);

        return ResponseEntity.status(201)
                .body(courseService.createCourse(request, email, role));
    }

    // 🔹 Public catalog
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    // 🔹 Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // 🔹 Search
    @GetMapping("/search")
    public ResponseEntity<List<Course>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    // 🔹 Filter
    @GetMapping("/filter")
    public ResponseEntity<List<Course>> filter(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level) {

        if (category != null) return ResponseEntity.ok(courseService.filterByCategory(category));
        if (level != null) return ResponseEntity.ok(courseService.filterByLevel(level));

        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    // 🔹 My Courses (Instructor)
    @GetMapping("/my")
    public ResponseEntity<List<Course>> myCourses(
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(courseService.getCoursesByInstructor(email));
    }

    // 🔹 Update Course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(courseService.updateCourse(id, request, email, role));
    }

    // 🔹 Submit for review
    @PutMapping("/{id}/submit")
    public ResponseEntity<String> submitForReview(@PathVariable Long id) {
        courseService.publishCourse(id);
        return ResponseEntity.ok("Course submitted for review");
    }

    // 🔹 Admin approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        courseService.approveCourse(id);
        return ResponseEntity.ok("Course approved");
    }

    // 🔹 Admin reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> reject(@PathVariable Long id) {
        courseService.rejectCourse(id);
        return ResponseEntity.ok("Course rejected");
    }

    // 🔹 Pending courses (Admin)
    @GetMapping("/pending")
    public ResponseEntity<List<Course>> getPendingCourses() {
        return ResponseEntity.ok(courseService.getPendingCourses());
    }

    // 🔹 Delete course
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        courseService.deleteCourse(id, email, role);
        return ResponseEntity.ok("Course deleted");
    }

    // 🔹 Featured
    @GetMapping("/featured")
    public ResponseEntity<List<Course>> getFeaturedCourses() {
        return ResponseEntity.ok(courseService.getFeaturedCourses());
    }

    // 🔹 Price filter
    @GetMapping("/price")
    public ResponseEntity<List<Course>> filterByPrice(@RequestParam Double maxPrice) {
        return ResponseEntity.ok(courseService.filterByPrice(maxPrice));
    }
}