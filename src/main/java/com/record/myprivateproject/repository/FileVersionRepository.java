package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileVersionRepository extends JpaRepository<FileVersion, Long>{
    Integer countByFile(FileEntry file);
}
