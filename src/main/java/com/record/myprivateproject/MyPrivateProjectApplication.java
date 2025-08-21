package com.record.myprivateproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.record.myprivateproject")
@EnableJpaRepositories(basePackages = "com.record.myprivateproject.repository")
@EntityScan(basePackages = "com.record.myprivateproject.domain")
public class MyPrivateProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyPrivateProjectApplication.class, args);
    }
}
