package com.record.myprivateproject.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "files")
public class FileEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_version_id")
    private FileVersion latestVersion;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(columnDefinition = "bigint default 0")
    private Long size = 0L;

    protected FileEntry() {}
    public FileEntry(Folder folder, String name, String contentType) {
        this.folder = folder;
        this.name = name;
        this.contentType = contentType;
    }

    public Long getId() {
        return id;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public FileVersion getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(FileVersion latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}