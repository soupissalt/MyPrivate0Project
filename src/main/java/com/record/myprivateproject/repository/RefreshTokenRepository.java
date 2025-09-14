package com.record.myprivateproject.repository;

import com.record.myprivateproject.domain.RefreshToken;
import com.record.myprivateproject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query(value = """
        select *
        from refresh_tokens
        where token_hash = UNHEX(SHA2(?1, 256))
        and revoked = 0
        and expires_at > now()
        order by id desc
        limit 1
    """, nativeQuery = true)
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
    long deleteByUserAndRevokedTrue(User user);
    long deleteByExpiresAtBefore(LocalDateTime date);
    @Modifying
    @Query(value = "delete from refresh_tokens where user_id = :userId",nativeQuery = true)
    void deleteByUserId(Long userId);
    long deleteByExpiresAt(LocalDateTime date);
    Optional<RefreshToken> findByTokenHash(byte[] tokenHash);
}
