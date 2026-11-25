package com.demo.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Web MVC 配置：全局 CORS，读取 application.yml 的 cors 节点
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class WebMvcConfig implements WebMvcConfigurer {

    /** 允许的来源（开发环境：Vite dev server 5173） */
    private List<String> allowedOrigins = new ArrayList<>();

    /** 允许的 HTTP 方法 */
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

    /** 允许的请求头 */
    private String allowedHeaders = "*";

    /** 是否允许携带 cookie */
    private boolean allowCredentials = true;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }
}
