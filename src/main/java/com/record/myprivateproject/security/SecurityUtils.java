package com.record.myprivateproject.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}
    public static String currentUsernameOrThrow(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getName() == null) throw new IllegalStateException("잘못된 계정");
        return auth.getName(); // = email
    }
}
