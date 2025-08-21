package com.record.myprivateproject.controller;

import com.record.myprivateproject.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final UserRepository userRepository;
    public HealthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/health")
    public String health() {
        long count = userRepository.count();
        return "ok(users="+count+")";
    }
}
