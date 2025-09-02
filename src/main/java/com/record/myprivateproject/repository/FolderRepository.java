package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByRepositoryAndParentIsNull(RepositoryEntity repository); //루트
    List<Folder> findByParentOrderByIdAsc(Folder parent);
    boolean existsByRepositoryIdAndParentIsNull(Long repositoryId);
    Optional<Folder> findByRepositoryIdAndParentIsNull(Long repositoryId);
}
