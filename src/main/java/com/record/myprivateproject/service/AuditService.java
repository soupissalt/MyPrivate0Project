package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.AuditLog;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.repository.AuditLogRepository;
import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditService {

    private final AuditLogRepository aurepo;
    private final AuthContext auth;
    private final UserRepository userRepo;

    public AuditService(AuditLogRepository aurepo, AuthContext auth, UserRepository userRepo) {
        this.aurepo = aurepo;
        this.auth = auth;
        this.userRepo = userRepo;
    }
    public void record(String action, String targetType, Long targetId, String detail){
        Long userId = auth.currentUserId();
        User actor = userRepo.getReferenceById(userId);
        aurepo.save(new AuditLog(actor, action, targetType, targetId, detail));
    }

    public void recordAs(Long actorUserId, String action, String targetType, Long targetId, String detail){
        User actor = (actorUserId != null) ? userRepo.getReferenceById(actorUserId) : null;
        aurepo.save(new AuditLog(actor, action, targetType, targetId, detail));
    }
    @Transactional(readOnly = true)
    public List<AuditLog> list(String targetType, Long targetId){
        return  aurepo.findByTargetTypeAndTargetIdOrderByIdDesc(targetType, targetId);
    }
}
