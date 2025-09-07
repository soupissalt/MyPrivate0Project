package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.RefreshToken;
import com.record.myprivateproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
    long deleteByUserAndRevokedTrue(User user);
    long deleteByExpiresAtBefore(LocalDateTime date);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
    long deleteByExpiresAt(LocalDateTime date);
}
