package com.record.myprivateproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password  // User 엔티티에 맞춰 필드명/유무 조정
) {}