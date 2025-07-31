package com.record.myprivateproject;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Month.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class MyPrivateProjectConfig {

    @Bean
    CommandLineRunner commandLineRunner(
            MemberRepository memberRepository) {
        return args -> {
            MemberEntity admin = new MemberEntity(
                    "Chanho",
                    LocalDate.of(2000, AUGUST,13),
                    "Man",
                    "cksghdla1@naver.com",
                    LocalDateTime.now()
            );
            MemberEntity member = new MemberEntity(
                    1L,
                    "Eunji",
                    LocalDate.of(2002, OCTOBER,21),
                    "WOMAN",
                    "eungi71004@naver.com",
                    LocalDateTime.now()
            );

            memberRepository.saveAll( //DB에 데이터 추가하기
                    List.of(admin, member)
            );
        };
    }
}
