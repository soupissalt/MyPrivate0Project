package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.RepositoryEntity;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class FolderService {
    private final FolderRepository folderRepo;
    private final RepositoryEntityRepository repoRepo;
    private final UserRepository userRepo;
    private final AuthContext authContext;
    private final RepositoryEntityRepository repositoryEntityRepository;

    public FolderService(FolderRepository folderRepo, RepositoryEntityRepository repoRepo, UserRepository userRepo, AuthContext authContext, RepositoryEntityRepository repositoryEntityRepository) {
        this.folderRepo = folderRepo;
        this.repoRepo = repoRepo;
        this.userRepo = userRepo;
        this.authContext = authContext;
        this.repositoryEntityRepository = repositoryEntityRepository;
    }

    @Transactional
    public Folder createFolder(Long repoId, Long parentId, String name) {
        final Long userId = authContext.currentUserId();
        RepositoryEntity repo = (repoId == null)
                ? getOrCreateDefaultRepo(userId)
                : repositoryEntityRepository
                .findByIdAndOwnerId(repoId, userId)
                .orElseThrow(()-> new IllegalArgumentException("repo를 찾을 수 없음"));

        Folder parent = null;
        if (parentId != null) {
            parent = folderRepo.findById(parentId)
                    .orElseThrow(()-> new IllegalArgumentException("parent 폴더를 찾을 수 없습니다."));
            if (!parent.getRepository().getId().equals(repo.getId())) {
                throw new IllegalArgumentException("상위폴더가 저장소에 존재하지 않습니다.");
            }
        }
        Folder folder = new Folder(repo, parent, name);
        folderRepo.save(folder);

        return folder;
    }

    @Transactional(readOnly = true)
    public List<Folder> children(Long folderId) {
        Folder parent = folderRepo.findById(folderId).orElseThrow();
        return folderRepo.findByParentOrderByIdAsc(parent);
    }

    private RepositoryEntity getOrCreateDefaultRepo(Long userId) {
        List<RepositoryEntity> existing = repoRepo.findByOwnerId(userId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        User owner = userRepo.getReferenceById(userId);
        RepositoryEntity repo = new RepositoryEntity(owner, "DefaultRepository", "private");
        return repoRepo.save(repo);
    }
}
