package com.record.myprivateproject.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String store(Long repoId, Long fileId, Integer versionNo, String filename, MultipartFile file) throws IOException;
}
