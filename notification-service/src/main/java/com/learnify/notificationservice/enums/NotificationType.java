package com.learnify.notificationservice.enums;

public enum NotificationType {

    // Student events
    ENROLLMENT_CONFIRMED,    // Student enrolled in a course
    PAYMENT_SUCCESS,         // Payment processed successfully
    PAYMENT_REFUNDED,        // Payment refunded by admin
    QUIZ_RESULT,             // Quiz submitted and graded
    CERTIFICATE_ISSUED,      // Course completion certificate generated
    COURSE_UPDATED,          // A course the student is enrolled in was updated

    // Instructor events
    COURSE_APPROVED,         // Admin approved instructor's course
    COURSE_REJECTED,         // Admin rejected instructor's course
    NEW_ENROLLMENT,          // A student enrolled in instructor's course
    NEW_REPLY,               // Someone replied to instructor's thread

    // General / Admin
    PLATFORM_ANNOUNCEMENT,   // Admin broadcast to all users
    SUBSCRIPTION_EXPIRING,   // Subscription expires soon (3 days warning)
    SUBSCRIPTION_EXPIRED,    // Subscription has expired
    ACCOUNT_SUSPENDED,       // Admin suspended user account

    // Forum events
    THREAD_REPLY,            // Someone replied to user's discussion thread
    REPLY_ACCEPTED           // User's reply was accepted as best answer
}