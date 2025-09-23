package com.record.myprivateproject.config;

import com.record.myprivateproject.exception.MDCLoggingFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMvcConfig {
    @Bean
    public FilterRegistrationBean<MDCLoggingFilter> mdcLoggingFilter() {
        FilterRegistrationBean<MDCLoggingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new MDCLoggingFilter());
        bean.setOrder(1);
        return bean;
    }
    @Bean
    public OpenAPI base(){
        return new OpenAPI()
                .info(new Info()
                        .title("My Project API")
                        .description("백엔드 포트폴리오 API 문서")
                        .version("v1.0"));
    }
    @Bean
    public GroupedOpenApi fileGroup(){
        return GroupedOpenApi.builder()
                .group("files")
                .pathsToMatch("/api/files/**")
                .build();
    }
    @Bean
    public OpenAPI openAPIWithSecurity(){
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
