package com.logsentinel.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web-level configuration — CORS policy, message converters, etc.
 * Kept deliberately minimal; extend as the project grows.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Allow any origin during development. In production, restrict this
     * to specific trusted domains using allowedOrigins("https://dashboard.example.com").
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*");
    }
}
