package com.clinic.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory, per-client-IP rate limiter implemented as a fixed one-minute
 * window counter. When a client exceeds {@code gateway.rate-limit.requests-per-minute}
 * the gateway short-circuits with HTTP 429 and a small JSON body.
 *
 * SUPERSEDED: the gateway now uses the Redis-backed Spring Cloud Gateway
 * RequestRateLimiter (see GatewayRateLimiterConfig + spring.cloud.gateway.default-filters),
 * so the limit is shared across instances. This in-memory implementation is kept
 * (with @Component removed, so it is NOT active) only as a quick revert option:
 * re-add @Component here and remove the default-filters block to fall back to it.
 */
public class RateLimitingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingGlobalFilter.class);

    private final int requestsPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitingGlobalFilter(
            @Value("${gateway.rate-limit.requests-per-minute:60}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = ClientIp.resolve(exchange);
        long currentMinute = System.currentTimeMillis() / 60_000L;

        // Atomically fetch-or-reset the window for this IP, then count this request.
        Window window = windows.compute(clientIp, (ip, existing) ->
                (existing == null || existing.minute != currentMinute) ? new Window(currentMinute) : existing);
        int count = window.count.incrementAndGet();

        if (count > requestsPerMinute) {
            log.warn("Rate limit exceeded for {} ({} > {} req/min) - returning 429",
                    clientIp, count, requestsPerMinute);
            return tooManyRequests(exchange);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":429,\"error\":\"Too Many Requests\","
                + "\"message\":\"Rate limit exceeded. Try again later.\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Runs early (after the logging filter at -100) so requests are rejected
        // before they are routed downstream.
        return -50;
    }

    /** Per-IP fixed-window counter. */
    private static final class Window {
        private final long minute;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long minute) {
            this.minute = minute;
        }
    }
}
