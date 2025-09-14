package com.record.myprivateproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeDtos {
    public record FileNode(Long id, String name, Long size, String contentType, Integer latestVersion) {}
    public record FolderNode(Long id, String name, Long parentId, List<FileNode> files, List<FolderNode> children) {}

    private Long id;
    private String name;
    private Long size;
    private String contentType;
    private Integer latestVersion;
    private Instant createdAt;
    private List<TreeDtos> children;
    public TreeDtos(Long id, String name, String contentType, List<TreeDtos> children) {
        this.id = id; this.name = name; this.contentType = contentType; this.children = children;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContentType() { return contentType; }
    public List<TreeDtos> getChildren() { return children; }

}
