package com.jerrycode.gym_services.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.app-dir}")
    private String appDir;

    @Value("${file.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fullPath = Paths.get(uploadDir, appDir).toAbsolutePath().normalize().toString();
        String resourceHandler = String.format("%s/%s/**", urlPrefix, appDir).replaceAll("/+", "/");
        registry.addResourceHandler(resourceHandler)
                .addResourceLocations("file:" + fullPath + "/")
                .setCachePeriod(3600);
    }
}