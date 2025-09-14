package com.record.myprivateproject.service;

import org.springframework.stereotype.Service;

@Service
public class SubjectService {
    public String hello() {
        return "과목 : 모바일 통합구현\n" +
                "학과 : 소프트웨어공학과\n" +
                "학번 : 20200675\n" +
                "이름 : 임찬호\n";
    }
}
