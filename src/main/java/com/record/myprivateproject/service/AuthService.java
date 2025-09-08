package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.*;
import com.record.myprivateproject.dto.AuthDtos.*;
import com.record.myprivateproject.dto.RegisterRequest;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.RefreshTokenRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final RepositoryEntityRepository repoRepo;
    private final FolderRepository folderRepository;

    public AuthService(
            UserRepository userRepo,
            RefreshTokenRepository rtRepo,
            PasswordEncoder encoder,
            AuthenticationManager authManager,
            JwtTokenProvider jwt,
            @Value("${app.jwt.refresh-ttl-seconds}")long refreshTtlSeconds, UserRepository userRepository, PasswordEncoder passwordEncoder, RepositoryEntityRepository repoRepo, FolderRepository folderRepository){
                this.userRepo = userRepo;
                this.encoder = encoder;
                this.rtRepo = rtRepo;
                this.authManager = authManager;
                this.jwt = jwt;
                this.refreshTtlSeconds = refreshTtlSeconds;
                this.passwordEncoder = passwordEncoder;
                this.repoRepo = repoRepo;
        this.folderRepository = folderRepository;
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
        //기존 토큰 전부 삭제 후 새 토큰 1개만 발급
        String refresh = issueRefreshToken(user);
        return new TokenResponse(access, refresh);
    }

    @Transactional(readOnly = true)
    public MeResponse me (String email){
        User user = userRepo.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new MeResponse(user.getEmail(), user.getRole().name());
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req){
        RefreshToken refreshToken = rtRepo.findByTokenAndRevokedFalse(req.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 로그인 접근 방법 입니다!"));
        //만료 체크
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            //만료된 건 바로 삭제 (누적 방지)
            rtRepo.deleteByUserId(refreshToken.getUser().getId());
            throw new IllegalArgumentException("토큰이 만료되었습니다! 새로 발급 받으세요!");
        }

        User user = refreshToken.getUser();
        rtRepo.deleteByUserId(user.getId());

        //해당 사용자 토큰 전량 삭제 후 새 토큰 1개만 발급(회전)
        String newRefresh = issueRefreshToken(user);
        String access = jwt.createAccessToken(user.getEmail(), Map.of("role", user.getRole().name()));
        return new TokenResponse(access, newRefresh);
    }
    @Transactional
    public void logout(String refreshToken){
        rtRepo.findByTokenAndRevokedFalse(refreshToken).ifPresent(rt -> {
            rtRepo.deleteByUserId(rt.getUser().getId());
            rt.setRevoked(true);
            rtRepo.save(rt);
        });
    }
    // import들: UserRepository, PasswordEncoder, User, RegisterRequest, LoginRequest, TokenResponse 등 프로젝트에 맞게
    @Transactional
    public TokenResponse register(RegisterRequest req) {
        // 중복 이메일 사전 체크
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        // 사용자 생성/저장
        User user = new User(req.email(), encoder.encode(req.password()), Role.READER);
        userRepo.save(user);
        /* 기본 레포지토리 보장
            repositoryEntity 기본 생성자/세터 금지 -> 공개 생성자 사용
            엔티티에 isDefault 필드가 있고 setDefault가 없으면 생성자에서만 설정
        */
        RepositoryEntity repo = repoRepo.findFirstByOwnerIdOrderByIdAsc(user.getId())
                .orElseGet(() -> repoRepo.save(
                        new RepositoryEntity(user, "Default Repository", "private")
                ));

        // root folder 보장
        folderRepository.findByRepositoryAndParentIsNull(repo)
                .stream()
                .findFirst()
                .orElseGet(() -> folderRepository.save(new Folder(repo, null, "root")));

        return login(new LoginRequest(req.email(), req.password()));
    }

    private String issueRefreshToken(User user){
        // 기존 토큰 모두 삭제 -> 누적 방지
        rtRepo.deleteByUserId(user.getId());

        String value = UUID.randomUUID().toString();
        LocalDateTime exp = LocalDateTime.now().plusSeconds(14);
        RefreshToken refreshToken = new RefreshToken(user, value, exp);
        rtRepo.save(refreshToken);
        return value;
    }
}
