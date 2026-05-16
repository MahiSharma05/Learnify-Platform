package com.learnify.authservice.service;

import com.learnify.authservice.entity.OtpVerification;
import com.learnify.authservice.entity.User;
import com.learnify.authservice.repository.OtpVerificationRepository;
import com.learnify.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void generateAndSendOtp(String email) {

        SecureRandom random = new SecureRandom();

        String otp = String.format("%06d",
                random.nextInt(999999));

        otpRepository.deleteByEmail(email);

        OtpVerification otpEntity = new OtpVerification();

        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);

        otpEntity.setExpiryTime(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {

        OtpVerification savedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("OTP not found"));

        if (savedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!savedOtp.getOtp().trim().equals(otp.trim())) {

            System.out.println("DB OTP: " + savedOtp.getOtp());

            System.out.println("ENTERED OTP: " + otp);

            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setEmailVerified(true);

        userRepository.save(user);

        otpRepository.delete(savedOtp);
    }
}