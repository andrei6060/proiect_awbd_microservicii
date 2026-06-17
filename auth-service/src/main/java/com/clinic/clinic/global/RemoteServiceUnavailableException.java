package com.clinic.clinic.global;

/**
 * Thrown by a circuit-breaker fallback when a required downstream service
 * (e.g. db-service) is unavailable / failing and the operation cannot complete
 * with a safe default. The GlobalExceptionHandler maps it to HTTP 503 so the
 * frontend receives a clean JSON error instead of a stack trace.
 */
public class RemoteServiceUnavailableException extends RuntimeException {

    public RemoteServiceUnavailableException(String service, Throwable cause) {
        super("Downstream service '" + service + "' is currently unavailable", cause);
    }
}
