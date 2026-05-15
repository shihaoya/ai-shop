package com.sh.aishop.config;

import com.sh.aishop.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private UploadConfig uploadConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/file/view/**",
                    "/api/file/**/*.jpg",
                    "/api/file/**/*.jpeg",
                    "/api/file/**/*.png",
                    "/api/file/**/*.gif",
                    "/api/file/**/*.webp",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态文件访问路径 /api/file/view/** -> /uploads/**
        String localPath = uploadConfig.getLocal().getBasePath();
        String accessPath = uploadConfig.getLocal().getAccessPath();
        registry.addResourceHandler(accessPath + "**")
                .addResourceLocations("file:" + localPath + "/");
    }
}