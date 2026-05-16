package com.learnify.authservice.service;

public interface OtpService {

    void generateAndSendOtp(String email);

    void verifyOtp(String email, String otp);
}