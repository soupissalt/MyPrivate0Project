package com.record.myprivateproject.controller;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.dto.FolderDtos.*;
import com.record.myprivateproject.service.FileService;
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
    // 폴더 하나에 대한 하위 폴더 + 파일 묶음
    @GetMapping("/{folderId}/contents")
    public ResponseEntity<FolderService.Contents> contents(@PathVariable Long folderId) {
        return ResponseEntity.ok(folderService.contents(folderId));
    }

    // 저장소의 루트 폴더 기준 묶음
    @GetMapping("/repo/{repoId}/contents")
    public ResponseEntity<FolderService.Contents> rootContents(@PathVariable Long repoId) {
        return ResponseEntity.ok(folderService.rootContents(repoId));
    }


    @PostMapping
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody CreateFolderRequest req){
        Folder folder = folderService.createFolder(req.repoId(), req.parentId(), req.name());
        Long pid = (folder.getParent() == null) ? null : folder.getParent().getId();
        return ResponseEntity.ok(new FolderResponse(folder.getId(), folder.getName(), pid));
    }

    @GetMapping("/{folderId}/children")
    public ResponseEntity<List<FolderResponse>> children(@PathVariable Long folderId){
        var list = folderService.children(folderId).stream()
                .map(f -> new FolderResponse(f.getId(), f.getName(), f.getParent() == null ? null : f.getParent().getId()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/root")
    public ResponseEntity<List<FolderResponse>> rootFolder(){
        var list = folderService.rootFolders().stream()
                .map(f -> new FolderResponse(f.getId(), f.getName(), null))
                .toList();
        return ResponseEntity.ok(list);
    }
}
