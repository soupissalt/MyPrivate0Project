package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.FileEntry;
import com.record.myprivateproject.domain.FileVersion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface FileVersionRepository extends JpaRepository<FileVersion, Long>{
    Integer countByFile(FileEntry file);

    @EntityGraph(attributePaths = "createdBy") // ★ 추가
    List<FileVersion> findByFileOrderByVersionNoDesc(FileEntry file);

    @EntityGraph(attributePaths = "createdBy") // ★ 추가
    List<FileVersion> findByFileIdOrderByVersionNoDesc(Long fileId);
    Optional<FileVersion> findByFileAndVersionNo(FileEntry file, Integer versionNo);

    interface FileVersionRow{
        Integer getVersionNo();
        Long getSize();
        String getChecksum();
        Timestamp getCreatedAt();
        String getCreatedBy();
    }
    @Query(value = "CALL get_file_versions(:fileId)", nativeQuery = true)
    List<FileVersionRow> callGetFileVersions(Long fileId);
}
