package com.wkr.storeserver.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

/**
 * 为高频但允许短暂延迟的数据配置更短的 Redis 缓存时间。
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration("dashboard:overview", cacheFor(Duration.ofSeconds(30)))
                .withCacheConfiguration("permission:model", cacheFor(Duration.ofMinutes(5)))
                .withCacheConfiguration("permission:codes", cacheFor(Duration.ofSeconds(30)))
                .withCacheConfiguration("permission:rules", cacheFor(Duration.ofSeconds(30)));
    }

    private RedisCacheConfiguration cacheFor(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues();
    }
}
