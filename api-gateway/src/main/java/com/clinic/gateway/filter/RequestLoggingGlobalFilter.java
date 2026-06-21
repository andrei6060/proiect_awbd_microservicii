package com.clinic.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Logs each request (method + path + client IP) and the response (status + elapsed
 * time) at INFO, and stamps a correlation id (X-Request-Id) onto the downstream
 * request and the response so a call can be traced across services.
 *
 * Deliberately does NOT log the Authorization header value or any request/response
 * body. The Authorization header is left untouched and forwarded downstream so the
 * services can still validate the JWT themselves.
 */
@Component
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingGlobalFilter.class);

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Reuse an inbound correlation id if present, otherwise generate one.
        String existing = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        final String requestId = (existing != null && !existing.isBlank())
                ? existing
                : UUID.randomUUID().toString();

        // Add the id to the downstream request and echo it on the response.
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> h.set(REQUEST_ID_HEADER, requestId)))
                .build();
        mutated.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        final String method = request.getMethod().name();
        final String path = request.getURI().getRawPath();
        final String clientIp = ClientIp.resolve(mutated);
        final long start = System.currentTimeMillis();

        log.info("--> {} {} from {} [requestId={}]", method, path, clientIp, requestId);

        return chain.filter(mutated).doFinally(signal -> {
            long elapsedMs = System.currentTimeMillis() - start;
            var status = mutated.getResponse().getStatusCode();
            log.info("<-- {} {} status={} {}ms [requestId={}]",
                    method, path, status != null ? status.value() : "n/a", elapsedMs, requestId);
        });
    }

    @Override
    public int getOrder() {
        // Outermost: run before routing (and before the rate limiter) so every
        // request - including rejected ones - is logged with its final status.
        return -100;
    }
}
