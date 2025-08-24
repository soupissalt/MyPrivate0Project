package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByRepositoryAndParentIsNull(RepositoryEntity repo); //루트
    List<Folder> findByParentOrderByIdAsc(Folder parent);
}
