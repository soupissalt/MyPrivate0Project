package com.record.myprivateproject.controller;

import com.record.myprivateproject.domain.AuditLog;
import com.record.myprivateproject.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    public record AuditResponse(Long id, String action, String targetType, Long targetId, String detail) {
        static AuditResponse from(AuditLog l) {
            return new AuditResponse(l.getId(), l.getAction(), l.getTargetType(), l.getTargetId(), l.getDetail());
        }
    }

    @GetMapping
    public ResponseEntity<List<AuditResponse>> list(@RequestParam String targetType, @RequestParam Long targetId) {
        var list = auditService.list(targetType, targetId).stream().map(AuditResponse::from).toList();
        return ResponseEntity.ok(list);
    }
}
