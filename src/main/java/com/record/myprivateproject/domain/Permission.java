package com.record.myprivateproject.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private RepositoryEntity repository;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User grantee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PermissionType permission; //READ, WRITE, OWNER`
    protected Permission() {}

    public Permission(RepositoryEntity repository, User grantee, PermissionType permission){
        this.repository = repository;
        this.grantee = grantee;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }
    public RepositoryEntity getRepository() {
        return repository;
    }
    public User getGrantee() {
        return grantee;
    }
    public PermissionType getPermission(){
        return permission;
    }
}
