package com.example.shop.cart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!redis")
public class InMemoryCartStore implements CartStore {
    private final Map<Long, Map<Long, Integer>> carts = new ConcurrentHashMap<>();

    @Override
    public void put(Long userId, Long productId, int quantity) {
        carts.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>()).put(productId, quantity);
    }

    @Override
    public void remove(Long userId, Long productId) {
        Map<Long, Integer> cart = carts.get(userId);
        if (cart != null) {
            cart.remove(productId);
        }
    }

    @Override
    public Map<Long, Integer> load(Long userId) {
        return new HashMap<>(carts.getOrDefault(userId, Map.of()));
    }

    @Override
    public void clear(Long userId) {
        carts.remove(userId);
    }
}
