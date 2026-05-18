package com.learnify.authservice.service;

public interface EmailService {

    void sendOtpEmail(String toEmail, String otp);
}