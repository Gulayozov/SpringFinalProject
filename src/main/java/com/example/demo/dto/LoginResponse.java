package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}