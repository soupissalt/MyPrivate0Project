package com.record.myprivateproject.controller;

import com.record.myprivateproject.dto.AuthDtos.*;
import com.record.myprivateproject.dto.RegisterRequest;
import com.record.myprivateproject.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me (Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            email = ud.getUsername();
        }
        else {
            email = authentication.getName();
        }
        return ResponseEntity.ok(authService.me(email));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req){
        authService.singup(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req){
        return ResponseEntity.ok().body(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req){
        return ResponseEntity.ok().body(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequest req){
        authService.logout(req.refreshToken());
        return ResponseEntity.ok().build();
    }
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }
}
