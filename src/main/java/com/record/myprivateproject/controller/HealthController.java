package com.record.myprivateproject.controller;

import com.record.myprivateproject.repository.UserRepository;
import com.record.myprivateproject.service.SubjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final UserRepository userRepository;
    private final SubjectService subjectService;

    public HealthController(UserRepository userRepository, SubjectService subjectService) {
        this.userRepository = userRepository;
        this.subjectService = subjectService;
    }

    @GetMapping("/api/health")
    public String health() {
        long count = userRepository.count();
        return "ok(users="+count+")";
    }
    @GetMapping("/api/subject")
    public String subject() {
        return subjectService.hello();
    }
}
