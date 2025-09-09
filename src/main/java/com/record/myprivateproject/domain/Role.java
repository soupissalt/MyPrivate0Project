package com.record.myprivateproject.domain;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.record.myprivateproject.domain.PermissionType.*;

public enum Role {
    ADMIN(Set.of(
            READ,
            WRITE,
            OWNER
    )),
    MAINTAINER(Set.of(
            READ,
            WRITE
    )), READER(Set.of(
            READ
    ));

    private final Set<PermissionType> permissions;

    Role(Set<PermissionType> permissions) {
        this.permissions = permissions;
    }
    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = getAuthorities()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getAuthority()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
    public String toDb() { return name().toLowerCase(); }
    public static Role fromDb(String s) {
        return switch (s) {
            case "admin" -> ADMIN;
            case "maintainer" -> MAINTAINER;
            case "reader" -> READER;
            default -> throw new IllegalArgumentException("Unknown role: " + s);
        };
    }
}