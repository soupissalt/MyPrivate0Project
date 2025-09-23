package com.record.myprivateproject.dto;

import com.record.myprivateproject.domain.FileEntry;

public record FileSummaryDto(
        Long fileId,
        String name,
        Long size,
        Integer versionNo,
        String sha256,
        java.time.LocalDate createdAt
) {
    public static FileSummaryDto from(FileEntry e) {
        var v = e.getLatestVersion();
        return new FileSummaryDto(e.getId(), e.getName(), v.getSize(), v.getVersionNo(), v.getSha256(), e.getCreatedAt());
    }
}
