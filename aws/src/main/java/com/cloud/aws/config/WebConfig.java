package com.cloud.aws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/aws/**")
                .allowedOrigins("http://localhost:3000") // Vue 앱의 URL을 설정
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
