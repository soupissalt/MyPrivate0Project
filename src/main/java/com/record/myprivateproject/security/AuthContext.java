package com.record.myprivateproject.security;

import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {
    private final UserRepository userRepository;
    public AuthContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long currentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null){
            throw new IllegalStateException("인증정보가 없습니다.");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("현재 사용자 정보를 찾을 수 없습니다."));
        return user.getId();
    }
}
