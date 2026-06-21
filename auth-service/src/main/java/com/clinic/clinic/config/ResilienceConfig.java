package com.clinic.clinic.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Explicitly configures the Spring Cloud CircuitBreakerFactory's "db-service"
 * instance. The same values are also in application.yml (resilience4j.*), which
 * resilience4j-spring-boot3 uses to register the instance for the actuator health
 * indicator + Micrometer metrics. This Customizer guarantees the factory itself
 * applies the intended circuit-breaker and time-limiter settings (in particular it
 * overrides Spring Cloud's 1s default time limiter, which the blocking RestTemplate
 * calls would otherwise hit).
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> dbServiceCircuitBreakerCustomizer() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        // Coarse backstop; the real per-attempt timeout is the RestTemplate readTimeout (3s).
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(12))
                .build();

        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(timeLimiterConfig), "db-service");
    }
}
