package com.learnify.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String message;
    private String profilePicUrl;
    public AuthResponse(String token , Long userId , String fullName , String email , String role , String message){
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.message = message;
    }
    //Legacy constrctor kept for backend compatibiltity
    public AuthResponse(String token , String email , String role , String message){
        this.token = token;
        this.email = email;
        this.role = role;
        this.message = message;
    }
}