package com.clinic.clinic.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates centralized configuration + live refresh. The values are served by
 * the Config Server (config-repo/application.yml and business-service.yml).
 *
 * Because this bean is @RefreshScope, POSTing to /api/v1/actuator/refresh after
 * changing the property in config-repo makes GET /api/v1/config/message return the
 * new value WITHOUT restarting the service.
 */
@RestController
@RequestMapping("config")
@RefreshScope
public class ConfigDemoController {

    @Value("${app.message:(no app.message configured)}")
    private String message;

    @Value("${app.features.beta-enabled:false}")
    private boolean betaEnabled;

    @GetMapping("/message")
    public Map<String, Object> message() {
        return Map.of(
                "message", message,
                "betaEnabled", betaEnabled
        );
    }
}
