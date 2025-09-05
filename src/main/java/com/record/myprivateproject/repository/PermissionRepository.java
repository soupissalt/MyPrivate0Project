package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByRepositoryIdOrderByIdAsc(Long repoId);
    Optional<Permission> findByRepositoryIdAndGranteeId(Long repoId, Long granteeId);
}
