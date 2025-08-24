package com.record.myprivateproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FolderDtos {
    public record CreateFolderRequest(@NotNull Long repoId, Long parentId, @NotBlank String name) {}
    public record FolderResponse(Long id, String name, Long parentId){}
}
