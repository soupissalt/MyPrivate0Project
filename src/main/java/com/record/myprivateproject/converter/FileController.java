package com.record.myprivateproject.converter;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.dto.FileDtos.*;
import com.record.myprivateproject.security.SecurityUtils;
import com.record.myprivateproject.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam Long folderId,
                                                     @RequestParam("file") MultipartFile file) throws Exception {
        String email = SecurityUtils.currentUsernameOrThrow();
        FileEntry saved = fileService.upload(email, folderId, file);
        var latest = saved.getLatestVersion();
        return ResponseEntity.ok(new FileUploadResponse(
                saved.getId(),
                latest.getVersionNo(),
                latest.getSize(),
                latest.getSha256()
        ));
    }
}
