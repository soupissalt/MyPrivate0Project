package com.record.myprivateproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalStorageService implements StorageService {
    private final Path root;

    public LocalStorageService(@Value("${app.storage.root:${user.home}/mpp-storage}")String rootPath) {
        this.root = Paths.get(rootPath).toAbsolutePath().normalize();
    }
    @Override
    public String store(Long repoId, Long fileId, Integer versionNo, String filename, MultipartFile file) throws IOException {
        Path dir = root.resolve(String.valueOf(repoId))
                .resolve(String.valueOf(fileId))
                .resolve(String.valueOf(versionNo));
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toString();
    }
}
