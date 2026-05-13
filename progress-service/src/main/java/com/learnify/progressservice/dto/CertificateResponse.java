package com.learnify.progressservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CertificateResponse {

    private Long id;
    private Long studentId;
    private String studentEmail;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private String instructorName;
    private LocalDate completionDate;
    private LocalDateTime issuedAt;
    private String certificateUrl;

    // UUID for third-party verification
    private String verificationCode;

    // Full public URL — embed in QR code on certificate PDF
    private String verificationUrl;

    // Computed: is this certificate valid and verifiable?
    private boolean valid;
}