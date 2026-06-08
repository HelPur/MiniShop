package com.example.shop.lock;

import com.example.shop.common.BusinessException;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("redis")
public class RedisDistributedLock implements DistributedLock {
    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> T execute(String lockKey, Duration ttl, Supplier<T> action) {
        String value = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, value, ttl);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException("Resource is busy, please retry");
        }
        try {
            return action.get();
        } finally {
            String current = redisTemplate.opsForValue().get(lockKey);
            if (value.equals(current)) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
