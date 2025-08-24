package com.record.myprivateproject.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class RepoDtos {
    public record CreateRepoRequest(@NotBlank String name, @NotBlank String visibility) {}
    public record RepoResponse(Long id, String name, String visibility, LocalDateTime createdAt) {}
}
