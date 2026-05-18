package com.learnify.lessonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient   // Register with Eureka
@EnableFeignClients      // Enable Feign for inter-service calls
public class LessonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LessonServiceApplication.class, args);
    }
}