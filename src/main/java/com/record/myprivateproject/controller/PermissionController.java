package com.record.myprivateproject.controller;

import com.record.myprivateproject.domain.Permission;
import com.record.myprivateproject.domain.PermissionType;
import com.record.myprivateproject.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionService service;
    public PermissionController(PermissionService service) {
        this.service = service;
    }

    public record GrantRequest(Long repoId, String granteeEmail, PermissionType permission) {}
    public record PermissionResponse(Long iod,Long repoId, String granteeEmail, String permission) {
        static PermissionResponse from(Permission p){
            return new PermissionResponse(
                    p.getId(),
                    p.getRepository().getId(),
                    p.getGrantee().getEmail(),
                    p.getPermission().name()
            );
        }
    }
    @PostMapping
    public ResponseEntity<PermissionResponse> grant(@RequestBody GrantRequest req){
        var p = service.grant(req.repoId(), req.granteeEmail(), req.permission());
        return ResponseEntity.ok(PermissionResponse.from(p));
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> list(@RequestParam Long repoId){
        var list = service.list(repoId).stream().map(PermissionResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id){
        service.revoke(id);
        return ResponseEntity.noContent().build();
    }
}
