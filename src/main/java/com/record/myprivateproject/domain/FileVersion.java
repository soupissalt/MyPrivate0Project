package com.record.myprivateproject.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_versions",
uniqueConstraints = @UniqueConstraint(name = "uk_fv_file_ver", columnNames = {"file_id", "version_no"}))
public class FileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntry file;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "storage_path", nullable = false, length = 512)
    private String storagePath;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long size = 0L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected FileVersion() {}
    public FileVersion(FileEntry file, Integer versionNo, String storagePath, String sha256, Long size, User createdBy) {
        this.file = file;
        this.versionNo = versionNo;
        this.storagePath = storagePath;
        this.sha256 = sha256;
        this.size = size;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public FileEntry getFile() {
        return file;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getSha256() {
        return sha256;
    }

    public Long getSize() {
        return size;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
