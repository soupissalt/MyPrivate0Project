package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileEntryRepository extends JpaRepository<FileEntry, Long> {
    List<FileEntry> findByFolderOrderByIdAsc(Folder folder);
    Optional<FileEntry> findByFolderIdAndName(Long folderId, String name);
}
