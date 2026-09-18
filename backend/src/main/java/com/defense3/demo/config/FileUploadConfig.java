package com.defense3.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传配置
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig implements WebMvcConfigurer {

    private String path;
    private String signaturePath;
    private String templatePath;

    @PostConstruct
    public void init() {
        // 创建上传目录
        createDirectoryIfNotExists(path);
        createDirectoryIfNotExists(signaturePath);
        createDirectoryIfNotExists(templatePath);
    }

    private void createDirectoryIfNotExists(String dirPath) {
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("创建上传目录: {}", dirPath);
            }
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", dirPath, e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，使上传的文件可以通过URL访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path + "/");
    }
}
