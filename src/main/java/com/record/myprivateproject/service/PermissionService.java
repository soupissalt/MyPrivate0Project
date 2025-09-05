package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.Permission;
import com.record.myprivateproject.domain.PermissionType;
import com.record.myprivateproject.repository.PermissionRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permRepo;
    private final RepositoryEntityRepository repoRepo;
    private final UserRepository userRepo;
    private final AuthContext auth;

    public PermissionService(PermissionRepository permRepo, RepositoryEntityRepository repoRepo, UserRepository userRepo, AuthContext auth) {
        this.permRepo = permRepo;
        this.repoRepo = repoRepo;
        this.userRepo = userRepo;
        this.auth = auth;
    }

    public Permission grant(Long repoId, String granteeEmail, PermissionType type){
        Long ownerId = auth.currentUserId();
        var repo = repoRepo.findByIdAndOwnerId(repoId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("repo 값을 찾을 수 없습니다."));

        var grantee = userRepo.findByEmail(granteeEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        var existing = permRepo.findByRepositoryIdAndGranteeId(repoId, grantee.getId());

        if (existing.isPresent()) return existing.get();

        var p = new Permission(repo, grantee, type);
        return permRepo.save(p);
    }

    @Transactional(readOnly = true)
    public List<Permission> list(Long repoId) {
        Long ownerId = auth.currentUserId();
        repoRepo.findByIdAndOwnerId(repoId, ownerId)
                .orElseThrow(()-> new IllegalArgumentException("repo 값을 찾을 수 없습니다."));

        return permRepo.findByRepositoryIdOrderByIdAsc(repoId);
    }
    public void revoke(Long permissionId){
        permRepo.deleteById(permissionId);
    }
}
