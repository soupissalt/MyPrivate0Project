package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTargetTypeAndTargetIdOrderByIdDesc(String targetType, Long targetId);
}
