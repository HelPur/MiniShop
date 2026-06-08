package com.example.shop.cart;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("redis")
public class RedisCartStore implements CartStore {
    private final StringRedisTemplate redisTemplate;

    public RedisCartStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(Long userId, Long productId, int quantity) {
        redisTemplate.opsForHash().put(key(userId), productId.toString(), String.valueOf(quantity));
    }

    @Override
    public void remove(Long userId, Long productId) {
        redisTemplate.opsForHash().delete(key(userId), productId.toString());
    }

    @Override
    public Map<Long, Integer> load(Long userId) {
        return redisTemplate.opsForHash().entries(key(userId)).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.valueOf(entry.getKey().toString()),
                        entry -> Integer.valueOf(entry.getValue().toString())));
    }

    @Override
    public void clear(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return "cart:" + userId;
    }
}
