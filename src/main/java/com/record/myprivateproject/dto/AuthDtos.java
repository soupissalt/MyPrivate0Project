package com.record.myprivateproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record SignupRequest(
            @Email @NotBlank String email,
            @NotBlank String password
            ){}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ){}

    public record TokenResponse(
            String accessToken,
            String refreshToken
    ){}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ){}
    public static record MeResponse(String email, String Role){}
}
