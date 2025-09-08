package com.record.myprivateproject.dto;

import java.time.LocalDateTime;

public class FileDtos {
    public record FileUploadResponse(Long fileId, Integer versionNo, Long size, String sha256){}
}
