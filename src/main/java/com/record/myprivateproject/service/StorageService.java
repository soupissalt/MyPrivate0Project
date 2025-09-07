package com.record.myprivateproject.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface StorageService {
    String store(Long repoId, Long fileId, Integer versionNo, String filename, MultipartFile file) throws IOException;

    default Resource load(String storedPath){
        try {
            Path p = Paths.get(storedPath).normalize();
            if (!Files.exists(p)) {
                throw new IllegalArgumentException("Do not exist File!: "+ storedPath);
            }
            return new UrlResource(p.toUri());
        }catch (MalformedURLException e){
            throw new RuntimeException(e);
        }
    }
}
