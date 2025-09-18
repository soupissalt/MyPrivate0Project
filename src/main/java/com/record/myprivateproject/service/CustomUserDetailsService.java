package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.Role;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.exception.BusinessException;
import com.record.myprivateproject.exception.ErrorCode;
import com.record.myprivateproject.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"존재하지 않는 사용자 입니다!: " + email) {
                });
        String role ="ROLE_"+(user.getRole() == null ? Role.READER : user.getRole().name());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .accountExpired(false).accountLocked(false)
                .credentialsExpired(false).disabled(false).build();
    }
    private Collection<? extends GrantedAuthority> mapAuthorities(Role role) {
        return role.getAuthorities();
    }
}
