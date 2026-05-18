package com.learnify.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Learnify Email Verification OTP");

        message.setText(
                "Your Learnify OTP is: "
                        + otp
                        + "\n\nOTP valid for 5 minutes."
        );

        mailSender.send(message);
    }
}