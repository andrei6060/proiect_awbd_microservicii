package com.clinic.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Per-client-IP key resolver for the Redis-backed Spring Cloud Gateway
 * RequestRateLimiter (configured as a default filter in application.yml). The
 * token-bucket state now lives in Redis, so the limit is shared across gateway
 * instances (unlike the old in-memory filter).
 *
 * Redis-down behavior: the RedisRateLimiter fails OPEN (logs an error and allows
 * the request) rather than blocking traffic, so a Redis outage degrades to
 * "no rate limiting" instead of a hard outage.
 */
@Configuration
public class GatewayRateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var remote = exchange.getRequest().getRemoteAddress();
            String ip = (remote != null && remote.getAddress() != null)
                    ? remote.getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }
}
