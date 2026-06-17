package com.clinic.clinic.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wraps inter-service calls to db-service with Resilience4j fault tolerance using
 * the Spring Cloud CircuitBreaker programmatic API.
 *
 * Composition: <b>retry is INSIDE, the circuit breaker is OUTSIDE</b>. Each logical
 * call retries transient failures up to the configured attempts; only the final
 * outcome is recorded by the breaker (so a couple of retried blips don't trip it,
 * but a genuinely down/slow db-service does). The breaker + retry + time-limiter
 * are all configured in application.yml under resilience4j.* for the "db-service"
 * instance; the shared registries also feed the actuator health indicator and the
 * Micrometer/Prometheus metrics.
 */
@Component
public class DbServiceClient {

    /** Resilience4j instance name (matches resilience4j.*.instances.db-service in yaml). */
    public static final String DB_SERVICE = "db-service";

    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final RetryRegistry retryRegistry;

    public DbServiceClient(CircuitBreakerFactory<?, ?> circuitBreakerFactory, RetryRegistry retryRegistry) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.retryRegistry = retryRegistry;
    }

    /**
     * Runs a remote call protected by retry (inner) + circuit breaker (outer).
     *
     * @param remoteCall the actual db-service call (e.g. restTemplate.exchange(...))
     * @param fallback   invoked when the breaker is open or the call ultimately fails;
     *                   return a safe default (empty result) or throw a domain exception.
     */
    public <T> T call(Supplier<T> remoteCall, Function<Throwable, T> fallback) {
        Retry retry = retryRegistry.retry(DB_SERVICE);
        return circuitBreakerFactory.create(DB_SERVICE)
                .run(Retry.decorateSupplier(retry, remoteCall), fallback);
    }
}
