package com.record.myprivateproject.service;

import com.record.myprivateproject.domain.*;
import com.record.myprivateproject.dto.AuditAction;
import com.record.myprivateproject.dto.AuthDtos.*;
import com.record.myprivateproject.dto.RegisterRequest;
import com.record.myprivateproject.exception.BusinessException;
import com.record.myprivateproject.exception.ErrorCode;
import com.record.myprivateproject.repository.FolderRepository;
import com.record.myprivateproject.repository.RefreshTokenRepository;
import com.record.myprivateproject.repository.RepositoryEntityRepository;
import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.security.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepo,
            RefreshTokenRepository rtRepo,
            PasswordEncoder encoder,
            AuthenticationManager authManager,
            JwtTokenProvider jwt,
            @Value("${app.jwt.refresh-ttl-seconds}")long refreshTtlSeconds, UserRepository userRepository, PasswordEncoder passwordEncoder, RepositoryEntityRepository repoRepo, FolderRepository folderRepository, AuditService auditService){
                this.userRepo = userRepo;
                this.encoder = encoder;
                this.rtRepo = rtRepo;
                this.authManager = authManager;
                this.jwt = jwt;
                this.refreshTtlSeconds = refreshTtlSeconds;
                this.passwordEncoder = passwordEncoder;
                this.repoRepo = repoRepo;
        this.folderRepository = folderRepository;
        this.auditService = auditService;
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
        auditService.recordAs(
                user.getId(),
                AuditAction.LOGIN.name(),
                "AUTH",
                user.getId(),
                "email =" + user.getEmail()
        );
        return new TokenResponse(access, refresh);
    }

    @Transactional(readOnly = true)
    public MeResponse me (String email){
        User user = userRepo.findByEmail(email).orElseThrow(() ->
                new BusinessException(ErrorCode.TOKEN_EXPIRED, "토큰 만료되었습니다!" ) {
                });
        return new MeResponse(user.getEmail(), user.getRole().name());
    }

    @Transactional
    public TokenResponse refresh(@NotBlank String rawRefreshToken){
        RefreshToken refreshToken = rtRepo.findByTokenHashAndRevokedFalse(rawRefreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID,"잘못된 로그인 접근 방법 입니다!") {
                });
        //만료 체크
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            //만료된 건 바로 삭제 (누적 방지)
            rtRepo.deleteByUserId(refreshToken.getUser().getId());
            throw  new BusinessException(ErrorCode.TOKEN_EXPIRED, "토큰 만료되었습니다!") {
            };
        }

        User user = refreshToken.getUser();
        rtRepo.deleteByUserId(user.getId());

        //해당 사용자 토큰 전량 삭제 후 새 토큰 1개만 발급(회전)
        String newRefresh = issueRefreshToken(user);
        String access = jwt.createAccessToken(user.getEmail(), Map.of("role", user.getRole().name()));
        auditService.recordAs(
                user.getId(),
                AuditAction.AUTH_REFRESH.name(),
                "AUTH",
                user.getId(),
                "refresh"
        );
        return new TokenResponse(access, newRefresh);
    }
    @Transactional
    public void logout(@NotBlank String refreshToken){
        RefreshToken rt = rtRepo.findByTokenHashAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_EXPIRED, "토큰 만료되었습니다!") {
                });

        Long userId = rt.getUser().getId();

        // 해당 사용자 토큰 전부 삭제(혹은 필요시 해당 토큰만 삭제로 바꿔도 됨)
        rtRepo.deleteByUserId(userId);
        auditService.recordAs(userId,
                AuditAction.LOGOUT.name(),
                "AUTH", userId,
                "logout"
        );
    }
    // import들: UserRepository, PasswordEncoder, User, RegisterRequest, LoginRequest, TokenResponse 등 프로젝트에 맞게
    @Transactional
    public TokenResponse register(RegisterRequest req) {
        // 중복 이메일 사전 체크
        if (userRepo.existsByEmail(req.email())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"이미 가입된 이메일입니다.") {
            };
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

    private String issueRefreshToken(User user) {
        rtRepo.deleteByUserId(user.getId());

        String raw = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime exp = LocalDateTime.now().plusDays(14);

        RefreshToken rt = new RefreshToken(user, raw, exp);
        rtRepo.save(rt);
        return raw; // 클라이언트에는 원본
    }

    private static String generateRefreshToken() {
        // UUID도 가능하지만 보안을 위해 32바이트 랜덤 권장
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
