package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.Folder;
import com.record.myprivateproject.domain.RepositoryEntity;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
import com.record.myprivateproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepoService {

    private final RepositoryEntityRepository repoRepo;
    private final UserRepository userRepo;
    private final FolderRepository folderRepo;

    public RepoService(RepositoryEntityRepository repoRepo, UserRepository userRepo, FolderRepository folderRepo) {
        this.repoRepo = repoRepo;
        this.userRepo = userRepo;
        this.folderRepo = folderRepo;
    }

    @Transactional
    public RepositoryEntity createRepository(String ownerEmail, String name, String visibility) {
        User owner = userRepo.findByEmail(ownerEmail).orElseThrow();
        repoRepo.findByOwnerIdAndName(owner.getId(), name).ifPresent(repo -> {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        });
        RepositoryEntity repo = new RepositoryEntity(owner, name, visibility);
        repoRepo.save(repo);

        //루트폴더 생성(parent = null)
        Folder root = new Folder(repo, null, "/");
        folderRepo.save(root);

        return repo;
    }

    @Transactional(readOnly = true)
    public List<RepositoryEntity> myRepositories(String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail).orElseThrow();
        return repoRepo.findByOwnerOrderByIdDesc(owner);
    }
}
