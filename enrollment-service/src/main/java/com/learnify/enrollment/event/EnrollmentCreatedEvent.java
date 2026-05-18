package com.learnify.enrollment.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentCreatedEvent implements Serializable{
    /** DB ID of the new Enrollment record */
    private Long enrollmentId;

    /** Student who enrolled */
    private Long studentId;

    /** Student's email (for email notification) */
    private String studentEmail;

    /** Student's display name */
    private String studentName;

    /** Course enrolled in */
    private Long courseId;

    /** Course title (for notification message) */
    private String courseTitle;

    /** Name of the instructor (for notification message) */
    private String instructorName;

    /** When the enrollment happened */
    private LocalDateTime enrolledAt;
}
