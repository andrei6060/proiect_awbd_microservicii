package com.clinic.clinic.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Logs which db-service instance (by local server port) served each inter-service
 * request. With two instances running on different ports, this makes the
 * Eureka/load-balancer round-robin visible in the logs.
 */
@Configuration
@Slf4j
public class ServedInstanceLoggingConfig implements WebMvcConfigurer {

    @Value("${server.port:unknown}")
    private String serverPort;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                log.info("Served {} {} on db-service instance port {}",
                        request.getMethod(), request.getRequestURI(), serverPort);
                return true;
            }
        }).addPathPatterns("/get/**", "/save/**", "/delete/**");
    }
}
