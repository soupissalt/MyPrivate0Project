package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.RepositoryEntity;
import com.record.myprivateproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, Long> {
    List<RepositoryEntity> findByOwnerOrderByIdDesc(User owner);
    Optional<RepositoryEntity> findByOwnerIdAndName(Long ownerId, String name);
    Optional<RepositoryEntity> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<RepositoryEntity> findFirstByOwnerIdOrderByIdAsc(Long ownerId);
    List<RepositoryEntity> findByOwnerId(Long userId);
}
