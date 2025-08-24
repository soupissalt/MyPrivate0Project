package com.record.myprivateproject.controller;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.dto.FolderDtos.*;
import com.record.myprivateproject.service.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderService folderService;
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody CreateFolderRequest req){
        Folder folder = folderService.createFolder(req.repoId(), req.parentId(), req.name());
        Long PID = folder.getParent() == null ? null : folder.getParent().getId();
        return ResponseEntity.ok(new FolderResponse(folder.getId(), folder.getName(), PID));
    }

    @GetMapping("/{folderId}/children")
    public ResponseEntity<List<FolderResponse>> children(@PathVariable Long folderId){
        var list = folderService.children(folderId).stream()
                .map(f -> new FolderResponse(f.getId(), f.getName(), f.getParent() == null ? null : f.getParent().getId()))
                .toList();
        return ResponseEntity.ok(list);
    }
}
