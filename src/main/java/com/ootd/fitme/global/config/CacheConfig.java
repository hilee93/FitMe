package com.ootd.fitme.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    // 별도의 spring.cache.type 값이 없을 경우 자동으로 caffeine 생성하도록 세팅하였습니다
    // (**별도 redis를 확인하고 싶으실 경우 꼭 프로퍼티 추가 부탁드립니다**)
    public CacheManager caffeineCacheManager() {
        log.info("[CacheConfig] Caffeine CacheManager 활성화");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
        );

        cacheManager.registerCustomCache("attributes",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());

        cacheManager.registerCustomCache("weatherforecast",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .recordStats()
                        .build());

        cacheManager.registerCustomCache("clothes_extraction",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .build());

        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory factory, RedisCacheConfiguration config) {
        log.info("[CacheConfig] Redis CacheManager 활성화");
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("attributes",
                        config.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("weatherforecast",  // NOTE: 이런식으로 자신 캐시이름, 세팅 아래 붙여서 추가
                        config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("clothes_extraction",
                        config.entryTtl(Duration.ofDays(1)))
                .enableStatistics()
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                        )
                )
                .prefixCacheNameWith("fitme:")
                .entryTtl(Duration.ofSeconds(600))
                .disableCachingNullValues();
    }
}