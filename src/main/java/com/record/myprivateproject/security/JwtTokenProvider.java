package com.record.myprivateproject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtParser parser;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl-seconds:900}") long accessTtlSec,
            @Value("${app.jwt.refresh-ttl-seconds:1209600}") long refreshTtlSec
    ) {
        // Base64 먼저 시도, 실패하면 평문으로 처리(32바이트 이상 필요)
        SecretKey key;
        try {
            key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException | DecodingException e) {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) { // 256-bit 이상
                throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits).");
            }
            key = Keys.hmacShaKeyFor(bytes);
        }
        this.secretKey = key;
        this.parser = Jwts.parserBuilder().setSigningKey(secretKey).build();
        this.accessTtlMs = accessTtlSec * 1000L;
        this.refreshTtlMs = refreshTtlSec * 1000L;
    }

    // 🔹 AuthService에서 사용하는 시그니처
    public String createAccessToken(String subject, Map<String, String> claims) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTtlMs))
                .signWith(secretKey, SignatureAlgorithm.HS256);

        if (claims != null && !claims.isEmpty()) {
            // Map<String, String> -> Map<String, Object>로 복사
            Map<String, Object> objClaims = new HashMap<>();
            objClaims.putAll(claims);
            builder.addClaims(objClaims);
        }
        return builder.compact();
    }
    public Jws<Claims> parse(String token) {
        return parser.parseClaimsJws(token);
    }
}
