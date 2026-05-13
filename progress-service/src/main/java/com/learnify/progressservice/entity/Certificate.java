package com.learnify.progressservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "certificates",
        uniqueConstraints = {
                // One certificate per student per course
                @UniqueConstraint(
                        name = "uk_student_course_cert",
                        columnNames = {"studentId", "courseId"}
                ),
                // Verification code must be globally unique
                @UniqueConstraint(
                        name = "uk_verification_code",
                        columnNames = {"verificationCode"}
                )
        }
)
@Data
@NoArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    private String studentEmail;
    private String studentName;

    @Column(nullable = false)
    private Long courseId;

    private String courseTitle;
    private String instructorName;

    // Date of course completion (shown on the certificate)
    @Column(nullable = false)
    private LocalDate completionDate;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    // URL to the generated certificate PDF (e.g. S3 link or base64 endpoint)
    private String certificateUrl;

    /**
     * Unique UUID for third-party verification.
     * Anyone can hit GET /api/certificates/verify/{code} to check validity
     * without needing to be logged in.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String verificationCode;

    // Full verification URL for QR code embedding
    private String verificationUrl;
}