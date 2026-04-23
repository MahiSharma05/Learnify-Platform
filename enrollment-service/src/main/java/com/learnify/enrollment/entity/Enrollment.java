package com.learnify.enrollment.entity;

import com.learnify.enrollment.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollments",
        // Prevent a student from enrolling in the same course twice
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_course",
                columnNames = {"studentId", "courseId"}
        )
)
@Data
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References user in auth-service (student's user ID)
    @Column(nullable = false)
    private Long studentId;

    // References course in course-service
    @Column(nullable = false)
    private Long courseId;

    // Denormalized for quick display (avoids cross-service call on every read)
    private String studentEmail;
    private String courseTitle;

    @CreationTimestamp
    private LocalDateTime enrolledAt;

    // Set when student reaches 100% progress
    private LocalDateTime completedAt;

    // ACTIVE, COMPLETED, CANCELLED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    // 0 to 100 — updated by progress-service or directly
    @Column(nullable = false)
    private Integer progressPercent = 0;

    // True once a certificate has been generated and issued
    @Column(nullable = false)
    private boolean certificateIssued = false;

    // URL to the generated certificate PDF (set after issuance)
    private String certificateUrl;
}