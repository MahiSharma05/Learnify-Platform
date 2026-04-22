package com.learnify.courseservice.service;

import com.learnify.courseservice.dto.CourseRequest;
import com.learnify.courseservice.entity.Course;

import java.util.List;

public interface CourseService {

    Course createCourse(CourseRequest request, String email, String role);

    Course getCourseById(Long id);

    List<Course> getAllPublishedCourses();

    List<Course> getCoursesByInstructor(String email);

    List<Course> searchCourses(String keyword);

    List<Course> filterByCategory(String category);

    List<Course> filterByLevel(String level);

    List<Course> filterByPrice(Double price);

    Course updateCourse(Long id, CourseRequest request, String email, String role);

    void publishCourse(Long id);

    void approveCourse(Long id);

    void rejectCourse(Long id);

    void deleteCourse(Long id, String email, String role);

    List<Course> getPendingCourses();

    List<Course> getFeaturedCourses();
}