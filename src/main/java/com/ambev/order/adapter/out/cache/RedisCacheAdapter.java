package com.ambev.order.adapter.out.cache;

import com.ambev.order.domain.port.out.OrderCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Adapter - Redis implementation of OrderCachePort
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements OrderCachePort {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean exists(String key) {
        log.debug("Adapter: Checking cache for key: {}", key);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void store(String key, String value, long ttl) {
        log.debug("Adapter: Storing in cache - key: {}, TTL: {} hours", key, ttl);
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.HOURS);
    }

    @Override
    public String get(String key) {
        log.debug("Adapter: Getting from cache - key: {}", key);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        log.debug("Adapter: Deleting from cache - key: {}", key);
        redisTemplate.delete(key);
    }
}

