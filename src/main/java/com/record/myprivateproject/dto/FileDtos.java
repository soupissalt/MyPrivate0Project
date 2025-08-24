package com.record.myprivateproject.dto;

public class FileDtos {
    public record FileUploadResponse(Long fileId, Integer versionNo, Long size, String sha256){}
}
