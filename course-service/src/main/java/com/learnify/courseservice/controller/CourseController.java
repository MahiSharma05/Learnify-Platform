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

    // Create Course
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.status(201)
                .body(courseService.createCourse(request, email, role));
    }

    // Get all courses
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // Search
    @GetMapping("/search")
    public ResponseEntity<List<Course>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    // Filter
    @GetMapping("/filter")
    public ResponseEntity<List<Course>> filter(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level) {

        if (category != null) return ResponseEntity.ok(courseService.filterByCategory(category));
        if (level != null) return ResponseEntity.ok(courseService.filterByLevel(level));

        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    // My courses
    @GetMapping("/my")
    public ResponseEntity<List<Course>> myCourses(
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(courseService.getCoursesByInstructor(email));
    }

    // Get instructor courses by instructorId
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<Course>> getInstructorCoursesById(
            @PathVariable Long instructorId) {

        return ResponseEntity.ok(
                courseService.getCoursesByInstructorId(instructorId)
        );
    }

    // Publish Course
    @PutMapping("/{id}/publish")
    public ResponseEntity<String> publishCourse(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        courseService.publishCourse(id);

        return ResponseEntity.ok("Course published successfully");
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(courseService.updateCourse(id, request, email, role));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role) {

        courseService.deleteCourse(id, email, role);
        return ResponseEntity.ok("Course deleted");
    }

    // ================= ADMIN APIs =================

    // Get all courses for admin
    @GetMapping("/admin/all")
    public ResponseEntity<List<Course>> getAllCoursesForAdmin() {

        return ResponseEntity.ok(
                courseService.getAllCoursesForAdmin()
        );
    }

    // Get pending courses
    @GetMapping("/admin/pending")
    public ResponseEntity<List<Course>> getPendingCourses() {

        return ResponseEntity.ok(
                courseService.getPendingCourses()
        );
    }

    // Approve course
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<String> approveCourse(
            @PathVariable Long id
    ) {

        courseService.approveCourse(id);

        return ResponseEntity.ok("Course approved");
    }

    // Reject course
    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<String> rejectCourse(
            @PathVariable Long id
    ) {

        courseService.rejectCourse(id);

        return ResponseEntity.ok("Course rejected");
    }
}