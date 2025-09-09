package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTargetTypeAndTargetIdOrderByIdDesc(String targetType, Long targetId);
    @Query("""
        select l from AuditLog l
        join fetch l.actor
        where l.targetType = :targetType and l.targetId = :targetId
        order by l.id desc
    """)
    List<AuditLog> findAllWithActor(String targetType, Long targetId);
}
