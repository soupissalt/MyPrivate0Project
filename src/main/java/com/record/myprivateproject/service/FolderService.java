package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.RepositoryEntity;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FolderService {
    private final FolderRepository folderRepo;
    private final RepositoryEntityRepository repoRepo;

    public FolderService(FolderRepository folderRepo, RepositoryEntityRepository repoRepo) {
        this.folderRepo = folderRepo;
        this.repoRepo = repoRepo;
    }

    @Transactional
    public Folder createFolder(Long repoId, Long parentId, String name) {
        RepositoryEntity repo = repoRepo.findById(repoId).orElseThrow();
        Folder parent = null;
        if (parentId != null) {
            parent = folderRepo.findById(parentId).orElseThrow();
            if (!parent.getRepository().getId().equals(repoId)) {
                throw new IllegalArgumentException("상위폴더가 저장소에 존재하지 않습니다.");
            }
        }
        Folder folder = new Folder(repo, parent, name);
        return folderRepo.save(folder);
    }

    @Transactional(readOnly = true)
    public List<Folder> children(Long folderId) {
        Folder parent = folderRepo.findById(folderId).orElseThrow();
        return folderRepo.findByParentOrderByIdAsc(parent);
    }
}
