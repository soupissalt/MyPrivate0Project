package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.RefreshToken;
import com.record.myprivateproject.domain.Role;
import com.record.myprivateproject.domain.User;
import com.record.myprivateproject.dto.AuthDtos.*;
import com.record.myprivateproject.repository.RefreshTokenRepository;
import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final RefreshTokenRepository rtRepo;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;
    private final long refreshTtlSeconds;

    public AuthService(
            UserRepository userRepo,
            RefreshTokenRepository rtRepo,
            PasswordEncoder encoder,
            AuthenticationManager authManager,
            JwtTokenProvider jwt,
            @Value("${app.jwt.refresh-ttl-seconds}")long refreshTtlSeconds){
                this.userRepo = userRepo;
                this.encoder = encoder;
                this.rtRepo = rtRepo;
                this.authManager = authManager;
                this.jwt = jwt;
                this.refreshTtlSeconds = refreshTtlSeconds;
    }

    @Transactional
    public void singup(SignupRequest req){
        if (userRepo.existsByEmail(req.email())) throw new IllegalArgumentException("이미 가입한적 있는 이메일 입니다!");
        User user = new User(req.email(), encoder.encode(req.password()), Role.READER); //Reader(기본 회원)
        userRepo.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest req){
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepo.findByEmail(req.email()).orElseThrow();
        String access = jwt.createAccessToken(user.getEmail(), Map.of("role", user.getRole().name()));
        String refresh = UUID.randomUUID().toString();
        LocalDateTime exp = LocalDateTime.now().plusSeconds(refreshTtlSeconds);
        rtRepo.save(new RefreshToken(user, refresh, exp));
        return new TokenResponse(access, refresh);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req){
        RefreshToken refreshToken = rtRepo.findByTokenAndRevokedFalse(req.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 로그인 접근 방법 입니다!"));
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            rtRepo.save(refreshToken);
            throw new IllegalArgumentException("토큰이 만료되었습니다! 새로 발급 받으세요!");
        }

        User user = refreshToken.getUser();
        String access = jwt.createAccessToken(user.getEmail(), Map.of("role", user.getRole().name()));
        refreshToken.setRevoked(true);
        rtRepo.save(refreshToken);
        String newRefresh = UUID.randomUUID().toString();
        LocalDateTime exp = LocalDateTime.now().plusSeconds(refreshTtlSeconds);
        rtRepo.save(new RefreshToken(user, newRefresh, exp));
        return new TokenResponse(access, newRefresh);
    }
    @Transactional
    public void logout(String refreshToken){
        rtRepo.findByTokenAndRevokedFalse(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            rtRepo.save(rt);
        });
    }
}
