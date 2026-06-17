package com.clinic.clinic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis-backed caching for read-heavy, rarely-changing medication lookups.
 *
 * NoSQL note: Redis here plays a DUAL role - it is our caching layer AND a NoSQL
 * key-value store (cached DTOs are stored as JSON values under string keys, e.g.
 * "medicationByName::Paracetamol"). If we later needed a document store for real
 * domain data, MongoDB would be the candidate (see the project notes).
 *
 * Serialization: String keys, GenericJackson2Json values (human-readable JSON in
 * redis-cli, and DTOs round-trip with embedded @class type info). Default JDK
 * serialization is deliberately NOT used.
 *
 * Resilience: a swallowing CacheErrorHandler means that if Redis is DOWN the app
 * still works - cache ops are skipped and reads fall through to the database
 * (graceful degradation). The app also starts fine without Redis (Lettuce connects
 * lazily on first use).
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String CACHE_ACTIVE = "medicationsActive";
    public static final String CACHE_AVAILABLE = "medicationsAvailable";
    public static final String CACHE_BY_NAME = "medicationByName";

    @Value("${app.cache.default-ttl-minutes:10}")
    private long defaultTtlMinutes;

    @Value("${app.cache.list-ttl-minutes:2}")
    private long listTtlMinutes;

    // Not created under the 'test' profile, where spring.cache.type=none yields a
    // no-op cache so the test suite needs no running Redis.
    @Bean
    @Profile("!test")
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtlMinutes))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Shorter TTL for the (quantity-sensitive) list caches, longer for by-id.
        Map<String, RedisCacheConfiguration> perCache = Map.of(
                CACHE_ACTIVE, defaultConfig.entryTtl(Duration.ofMinutes(listTtlMinutes)),
                CACHE_AVAILABLE, defaultConfig.entryTtl(Duration.ofMinutes(listTtlMinutes)),
                CACHE_BY_NAME, defaultConfig.entryTtl(Duration.ofMinutes(defaultTtlMinutes))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    /**
     * Degrade gracefully when Redis is unavailable: log a warning and continue
     * (treat as a cache miss / no-op eviction) instead of failing the request.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Redis cache GET failed on '{}' (key={}); serving from source. {}",
                        cache.getName(), key, ex.toString());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("Redis cache PUT failed on '{}' (key={}); skipping cache. {}",
                        cache.getName(), key, ex.toString());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Redis cache EVICT failed on '{}' (key={}). {}",
                        cache.getName(), key, ex.toString());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.warn("Redis cache CLEAR failed on '{}'. {}", cache.getName(), ex.toString());
            }
        };
    }
}
