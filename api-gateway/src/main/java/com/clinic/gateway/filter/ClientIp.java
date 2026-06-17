package com.clinic.gateway.filter;

import org.springframework.web.server.ServerWebExchange;

/**
 * Resolves the client IP for logging / rate limiting. Prefers the first
 * X-Forwarded-For hop (when behind a proxy/LB) and falls back to the socket
 * remote address.
 */
final class ClientIp {

    private ClientIp() {
    }

    static String resolve(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        var remote = exchange.getRequest().getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }
        return "unknown";
    }
}
