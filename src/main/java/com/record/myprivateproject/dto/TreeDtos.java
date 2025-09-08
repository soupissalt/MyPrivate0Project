package com.record.myprivateproject.dto;

import java.util.List;

public class TreeDtos {
    public record FileNode(Long id, String name, Long size, String contentType, Integer latestVersion) {}
    public record FolderNode(Long id, String name, Long parentId, List<FileNode> files, List<FolderNode> children) {}
}
