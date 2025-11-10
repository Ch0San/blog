package com.example.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 정적 리소스(업로드 파일) 매핑 설정
 */
/**
 * 정적 리소스(업로드 파일) 매핑 설정.
 *
 * `uploads` 디렉터리를 `/uploads/**` 경로로 노출합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toAbsolutePath().toUri().toString();
        // Spring ResourceHandler는 "file:" 리소스 위치에 마지막 슬래시(/)가 있어야 정상 동작합니다
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
