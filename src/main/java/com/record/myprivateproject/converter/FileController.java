package com.record.myprivateproject.converter;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.dto.FileDtos.*;
import com.record.myprivateproject.security.SecurityUtils;
import com.record.myprivateproject.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

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
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId,
                                                 @RequestParam(required = false) Integer version,
                                                 @AuthenticationPrincipal UserDetails principal)
            throws Exception {
        var d =fileService.prepareDownload(principal.getUsername(), fileId, version);

        ContentDisposition cd = ContentDisposition.attachment()
                .filename(d.resource().getFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.parseMediaType(d.contentType()))
                .contentLength(d.size())
                .body(d.resource());
    }
}
