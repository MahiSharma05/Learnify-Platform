package com.learnify.courseservice.repository;

import com.learnify.courseservice.entity.Course;
import com.learnify.courseservice.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByPublishedTrue();

    //  Changed from instructorId → email
    List<Course> findByInstructorName(String instructorName);

    List<Course> findByCategoryAndPublishedTrue(String category);

    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByLevelAndPublishedTrue(String level);

    List<Course> findByFeaturedTrueAndPublishedTrue();

    @Query("SELECT c FROM Course c WHERE c.published = true AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    List<Course> findByApprovalStatus(ApprovalStatus status);

    List<Course> findByPriceLessThanEqualAndPublishedTrue(Double price);
}